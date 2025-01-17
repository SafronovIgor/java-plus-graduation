package ru.practicum.priv.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.HttpStatsClient;
import ru.practicum.dto.StatResponseDto;
import ru.practicum.dto.event.*;
import ru.practicum.dto.event.request.*;
import ru.practicum.exception.ConflictException;
import ru.practicum.exception.NotFoundException;
import ru.practicum.model.Category;
import ru.practicum.model.Event;
import ru.practicum.model.Request;
import ru.practicum.model.User;
import ru.practicum.priv.repository.PrivateCategoryRepository;
import ru.practicum.priv.repository.PrivateEventRepository;
import ru.practicum.priv.repository.PrivateUserRepository;
import ru.practicum.priv.repository.RequestRepository;
import ru.practicum.util.StatParams;
import ru.practicum.util.Statistical;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class PrivateEventServiceImpl implements PrivateEventService {
    private final PrivateEventRepository privateEventRepository;
    private final PrivateUserRepository privateUserRepository;
    private final PrivateCategoryRepository privateCategoryRepository;
    private final RequestRepository requestRepository;
    private final NewEventDtoMapper newEventDtoMapper;
    private final EventFullDtoMapper eventFullDtoMapper;
    private final UpdateEventUserRequestMapper updateEventUserRequestMapper;
    private final HttpStatsClient httpStatsClient;
    private final RequestDtoMapper requestDtoMapper;

    @Transactional
    @Override
    public EventFullDto addEvent(Long userId, NewEventDto newEventDto) {
        User initiator =
                privateUserRepository.findById(userId)
                        .orElseThrow(() -> new RuntimeException("Not authorized to add new event"));
        Category category = privateCategoryRepository.findById(newEventDto.getCategoryId())
                .orElseThrow(() -> new RuntimeException("No such category"));
        Event event = newEventDtoMapper.toEvent(newEventDto, initiator, category, State.PENDING);
        event = privateEventRepository.save(event);
        Long confirmedRequests = 0L;
        Long views = 0L;
        return eventFullDtoMapper.toDto(event, confirmedRequests, views);
    }

    @Transactional(readOnly = true)
    @Override
    public List<EventShortDto> getMyEvents(Long userId, Integer from, Integer size) {
        Pageable pageable = PageRequest.of(from, size);
        List<EventShortDto> eventShortDtos = new ArrayList<>(privateEventRepository.getEvents(userId, pageable));
        log.info("found {} events", eventShortDtos.size());
        StatParams statParams = Statistical.getParams(new ArrayList<>(eventShortDtos));
        log.info("parameters for statService created: {}", statParams);
        List<StatResponseDto> statResponseDto =
                httpStatsClient.getStats(statParams.start(), statParams.end(), statParams.uriList(), true);
        Map<Long, Long> hitMap = statResponseDto
                .stream()
                .collect(Collectors.toMap(x -> Long.parseLong(x.getUri().split("/")[2]), StatResponseDto::getHits));
        eventShortDtos.sort(Comparator.comparingLong(EventShortDto::getId));
        if (hitMap.isEmpty()) {
            return eventShortDtos;
        }
        for (EventShortDto eventShortDto : eventShortDtos) {
            Long eventId = eventShortDto.getId();
            eventShortDto.setViews(hitMap.getOrDefault(eventId, 0L));
        }
        log.info("found {} eventShortDtos", eventShortDtos.size());
        return eventShortDtos;
    }

    @Transactional(readOnly = true)
    @Override
    public EventFullDto getMyEvent(Long userId, Long eventId) {
        Event event =
                privateEventRepository.findByIdAndInitiatorId(eventId, userId)
                        .orElseThrow(() -> new NotFoundException("Event not found: " + eventId));
        Long confirmedRequests =
                privateEventRepository.getRequestCountByEventAndStatus(eventId, Status.CONFIRMED).getConfirmedRequests();
        EventFullDto eventFullDto = eventFullDtoMapper.toDto(event, confirmedRequests, 0L);
        StatParams statParams = Statistical.getParams(List.of(eventFullDto));
        log.info("parameters for statService created: {}", statParams);
        List<StatResponseDto> statResponseDtos =
                httpStatsClient.getStats(statParams.start(), statParams.end(), statParams.uriList(), true);
        if (!statResponseDtos.isEmpty()) {
            eventFullDto.setViews(statResponseDtos.getFirst().getHits());
        }
        return eventFullDto;
    }

    @Transactional
    @Override
    public EventFullDto updateMyEvent(Long userId, Long eventId, UpdateEventUserRequest updateEventUserRequest) {
        Event event =
                privateEventRepository.findByIdAndInitiatorId(eventId, userId)
                        .orElseThrow(() -> new NotFoundException("Event not found: " + eventId));
        if (event.getState() == State.PUBLISHED) {
            throw new ConflictException("cannot modify in current state");
        }
        final Long categoryId = updateEventUserRequest.getCategoryId();
        Category category;
        if (categoryId == null) {
            category = event.getCategory();
        } else {
            category = privateCategoryRepository.findById(categoryId)
                    .orElseThrow(() -> new NotFoundException("Category not found: " + categoryId));
        }
        event = updateEventUserRequestMapper.updateEvent(updateEventUserRequest, event, category);
        UserStateAction userStateAction = updateEventUserRequest.getUserStateAction();
        log.info("userStateAction value: {}", userStateAction);
        switch (userStateAction) {
            case null:
                break;
            case SEND_TO_REVIEW:
                event.setState(State.PENDING);
                log.info("event state updated to {}", event.getState());
                break;
            case CANCEL_REVIEW:
                event.setState(State.CANCELED);
                break;
        }
        Long confirmedRequests =
                privateEventRepository.getRequestCountByEventAndStatus(eventId, Status.CONFIRMED).getConfirmedRequests();
        EventFullDto dto = eventFullDtoMapper.toDto(event, confirmedRequests, 0L);
        StatParams statParams = Statistical.getParams(List.of(dto));
        log.info("parameters for statService created: {}", statParams);
        List<StatResponseDto> statResponseDto =
                httpStatsClient.getStats(statParams.start(), statParams.end(), statParams.uriList(), true);
        Long hits = 0L;
        if (!statResponseDto.isEmpty()) {
            hits = statResponseDto.getFirst().getHits();
        }
        dto.setViews(hits);
        log.info("eventFullDto state returned: {}", dto.getState());
        return dto;
    }

    @Override
    @Transactional(readOnly = true)
    public List<ParticipationRequestDto> getMyEventRequests(Long userId, Long eventId) {
        boolean exists =
                privateEventRepository.existsByIdAndInitiatorId(eventId, userId);
        if (!exists) {
            throw new NotFoundException("Event not found: " + eventId);
        }
        return requestRepository.findDtosByEventId(eventId);
    }

    /**
     * Обратите внимание:
     * <ul>
     * <li>если для события лимит заявок равен 0 или отключена пре-модерация заявок, то подтверждение заявок не требуется</li>
     * <li>нельзя подтвердить заявку, если уже достигнут лимит по заявкам на данное событие (Ожидается код ошибки 409)</li>
     * <li>статус можно изменить только у заявок, находящихся в состоянии ожидания (Ожидается код ошибки 409)</li>
     * <li>если при подтверждении данной заявки, лимит заявок для события исчерпан, то все неподтверждённые заявки необходимо отклонить</li>
     * </ul>
     */

    @Transactional
    @Override
    public EventRequestStatusUpdateResult updateMyEventRequests(Long userId, Long eventId,
                                                                EventRequestStatusUpdateRequest updateRequest) {
        Event event =
                privateEventRepository.findByIdAndInitiatorId(eventId, userId)
                        .orElseThrow(() -> new NotFoundException("Event not found: " + eventId));


        return verifyAndUpdate(updateRequest.getRequestIds(),
                updateRequest.getStatus(), event);
    }

    private EventRequestStatusUpdateResult verifyAndUpdate(Set<Long> updateRequestIds,
                                                           Status updateRequestStatus,
                                                           Event event) {
        Long eventId = event.getId();
        Integer participantLimit = event.getParticipantLimit();
        if (participantLimit.equals(0) || event.getRequestModeration().equals(false)) {
            return new EventRequestStatusUpdateResult();
        }
        List<Request> allRequests = requestRepository.findAllByEventId(eventId);
        log.info("found all requests for event {}", allRequests);
        Map<Status, Map<Long, Request>> statusSetMap = allRequests.stream()
                .collect(Collectors.groupingBy(Request::getStatus, Collectors.toMap(Request::getId, Function.identity())));
        Map<Long, Request> pendingRequests = statusSetMap.get(Status.PENDING);
        log.info("pending requests are: {}", pendingRequests);
        if (pendingRequests == null || pendingRequests.isEmpty()) {
            throw new ConflictException("No pending requests for: " + eventId);
        }
        Set<Long> pendingRequestIds = pendingRequests.keySet();
        if (!pendingRequestIds.containsAll(updateRequestIds)) {
            throw new ConflictException("Cannot update, status not pending: " + eventId);
        }
        Set<ParticipationRequestDto> confirmed;
        Set<ParticipationRequestDto> rejected;
        switch (updateRequestStatus) {
            case CONFIRMED:
                Set<Long> confirmedRequests = statusSetMap.getOrDefault(Status.CONFIRMED, Map.of()).keySet();
                int confirmedSize = confirmedRequests.size();
                int total = updateRequestIds.size() + confirmedSize;
                if (total > participantLimit) {
                    throw new ConflictException("Requests limit exceeded");
                }
                requestRepository.updateAllByIds(updateRequestIds, updateRequestStatus);
                Map<Long, Request> confirmedCopy = new HashMap<>(pendingRequests);
                confirmedCopy.keySet().retainAll(updateRequestIds);
                Collection<Request> confirmedValues = confirmedCopy.values();
                confirmedValues.forEach(x -> x.setStatus(Status.CONFIRMED));
                confirmed =
                        requestDtoMapper.toParticipationRequestDtos(confirmedValues);
                rejected = Set.of();
                if (total == participantLimit) {
                    pendingRequestIds.removeAll(updateRequestIds);
                    requestRepository.updateAllByIds(pendingRequestIds, Status.REJECTED);
                    Collection<Request> pendingValues = pendingRequests.values();
                    pendingValues.forEach(x -> x.setStatus(Status.REJECTED));
                    rejected = requestDtoMapper.toParticipationRequestDtos(pendingValues);
                }
                return new EventRequestStatusUpdateResult(confirmed, rejected);
            case REJECTED:
                requestRepository.updateAllByIds(updateRequestIds, updateRequestStatus);
                Collection<Request> pendingValues = pendingRequests.values();
                pendingValues.forEach(x -> x.setStatus(Status.REJECTED));
                rejected =
                        requestDtoMapper.toParticipationRequestDtos(pendingValues);
                confirmed = Set.of();
                return new EventRequestStatusUpdateResult(confirmed, rejected);
            case null, default:
                throw new ConflictException("Incorrect update request status: " + updateRequestStatus);
        }
    }
}
