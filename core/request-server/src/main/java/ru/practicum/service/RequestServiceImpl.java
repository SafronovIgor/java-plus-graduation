package ru.practicum.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.client.event.EventClient;
import ru.practicum.client.user.UserClient;
import ru.practicum.dto.event.EventFullDto;
import ru.practicum.dto.request.ParticipationRequestDto;
import ru.practicum.enums.State;
import ru.practicum.enums.Status;
import ru.practicum.exception.IntegrityViolationException;
import ru.practicum.exception.NotFoundException;
import ru.practicum.mapper.RequestMapper;
import ru.practicum.model.Request;
import ru.practicum.repository.RequestsRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

@Slf4j
@Service
@RequiredArgsConstructor
public class RequestServiceImpl implements RequestService {
    private final RequestsRepository requestsRepository;
    private final RequestMapper requestMapper;
    private final UserClient userClient;
    private final EventClient eventClient;

    @Override
    public List<ParticipationRequestDto> findAllRequestsByUserId(long userId) {
        log.info("Finding all participation requests for user ID: {}", userId);
        verifyUserExists(userId);
        List<Request> requests = requestsRepository.findAllByRequesterId(userId);
        List<ParticipationRequestDto> result = requestMapper.listRequestToListParticipationRequestDto(requests);
        log.info("Found {} participation requests for user ID: {}", result.size(), userId);
        return result;
    }

    @Override
    @Transactional
    public ParticipationRequestDto addRequest(long userId, long eventId) {
        log.info("Adding request for user ID: {} and event ID: {}", userId, eventId);
        verifyRequestNotExists(eventId, userId);
        verifyUserExists(userId);
        verifyUserNotInitiator(eventId, userId);
        EventFullDto event = getEvent(eventId);
        verifyEventCanAcceptRequest(event, eventId);
        Request request = createNewRequest(userId, eventId, event);
        request = requestsRepository.save(request);
        ParticipationRequestDto result = requestMapper.requestToParticipationRequestDto(request);
        log.info("Successfully added request: {}", result);
        return result;
    }

    @Override
    @Transactional
    public ParticipationRequestDto cancelRequest(long userId, long requestId) {
        log.info("Canceling request ID: {} for user ID: {}", requestId, userId);
        verifyUserExists(userId);
        Request request = getRequestById(requestId);
        request.setStatus(Status.CANCELED);
        request = requestsRepository.save(request);
        ParticipationRequestDto result = requestMapper.requestToParticipationRequestDto(request);
        log.info("Successfully canceled request: {}", result);
        return result;
    }

    @Override
    public List<ParticipationRequestDto> findAllRequestsByEventId(Long eventId) {
        log.info("Finding all requests for event ID: {}", eventId);
        List<Request> requests = requestsRepository.findByEventId(eventId);
        List<ParticipationRequestDto> result = requestMapper.listRequestToListParticipationRequestDto(requests);
        log.info("Found {} requests for event ID: {}", result.size(), eventId);
        return result;
    }

    @Override
    public List<ParticipationRequestDto> findAllRequestsByRequestsId(Set<Long> requestsId) {
        log.info("Finding requests by IDs: {}", requestsId);
        List<Request> requests = requestsRepository.findByIdIn(requestsId);
        List<ParticipationRequestDto> result = requestMapper.listRequestToListParticipationRequestDto(requests);
        log.info("Found {} requests for provided IDs", result.size());
        return result;
    }

    @Override
    @Transactional
    public List<ParticipationRequestDto> updateRequest(Set<Long> requestsId, String status) {
        log.info("Updating requests: {} to status: {}", requestsId, status);
        List<Request> requests = requestsRepository.findByIdIn(requestsId);
        requests.forEach(request -> request.setStatus(Status.valueOf(status)));
        List<ParticipationRequestDto> result = requestMapper.listRequestToListParticipationRequestDto(requests);
        log.info("Successfully updated {} requests to status: {}", result.size(), status);
        return result;
    }

    @Override
    public boolean findExistRequests(Long eventId, Long userId, String status) {
        log.debug("Checking if request exists for event ID: {}, user ID: {}, status: {}", eventId, userId, status);
        boolean exists = requestsRepository.existsByEventIdAndRequesterIdAndStatus(eventId, userId, Status.valueOf(status));
        log.info("Request existence check result: {}", exists);
        return exists;
    }

    @Override
    public List<ParticipationRequestDto> findAllRequestsByEventIdAndStatus(Long eventId, String status) {
        log.info("Finding all requests for event ID: {} with status: {}", eventId, status);
        List<Request> requests = requestsRepository.findAllByStatusAndEventId(Status.valueOf(status), eventId);
        List<ParticipationRequestDto> result = requestMapper.listRequestToListParticipationRequestDto(requests);
        log.info("Found {} requests for event ID: {} with status: {}", result.size(), eventId, status);
        return result;
    }

    private void verifyUserExists(long userId) {
        log.debug("Verifying if user exists with ID: {}", userId);
        if (!userClient.getUserExists(userId)) {
            log.error("User with ID: {} does not exist", userId);
            throw new NotFoundException("User with id = " + userId + " not found");
        }
    }

    private void verifyRequestNotExists(long eventId, long userId) {
        log.debug("Checking if request already exists for event ID: {} and user ID: {}", eventId, userId);
        requestsRepository.findByEventIdAndRequesterId(eventId, userId).ifPresent(r -> {
            log.error("Request already exists for event ID: {} and user ID: {}", eventId, userId);
            throw new IntegrityViolationException("Request with userId " + userId + " and eventId " + eventId + " already exists");
        });
    }

    private void verifyUserNotInitiator(long eventId, long userId) {
        log.debug("Checking if user ID: {} is the initiator of event ID: {}", userId, eventId);
        if (eventClient.findExistEventByEventIdAndInitiatorId(eventId, userId)) {
            log.error("User ID: {} is the initiator of event ID: {}", userId, eventId);
            throw new IntegrityViolationException("UserId " + userId + " initiates eventId " + eventId);
        }
    }

    private EventFullDto getEvent(long eventId) {
        log.debug("Fetching event with ID: {}", eventId);
        return eventClient.findEventById(eventId);
    }

    private void verifyEventCanAcceptRequest(EventFullDto event, long eventId) {
        log.debug("Checking if event ID: {} can accept requests", eventId);

        if (!event.getState().equals(State.PUBLISHED)) {
            log.error("Event with ID: {} is not published", eventId);
            throw new IntegrityViolationException("Event with id = " + eventId + " is not published");
        }

        List<Request> confirmedRequests = requestsRepository.findAllByStatusAndEventId(Status.CONFIRMED, eventId);
        if (event.getParticipantLimit() > 0 && event.getParticipantLimit() == confirmedRequests.size()) {
            log.error("Request limit exceeded for event ID: {}", eventId);
            throw new IntegrityViolationException("Request limit exceeded");
        }
    }

    private Request createNewRequest(long userId, long eventId, EventFullDto event) {
        log.debug("Creating a new request for user ID: {} and event ID: {}", userId, eventId);
        Request request = new Request();
        request.setCreated(LocalDateTime.now());
        request.setRequesterId(userId);
        request.setEventId(eventId);

        if (event.getParticipantLimit() == 0 || !event.getRequestModeration()) {
            request.setStatus(Status.CONFIRMED);
        } else {
            request.setStatus(Status.PENDING);
        }
        return request;
    }

    private Request getRequestById(long requestId) {
        log.debug("Fetching request with ID: {}", requestId);
        return requestsRepository.findById(requestId)
                .orElseThrow(() -> {
                    log.error("Request with ID: {} not found", requestId);
                    return new NotFoundException("Request with id = " + requestId + " not found");
                });
    }
}