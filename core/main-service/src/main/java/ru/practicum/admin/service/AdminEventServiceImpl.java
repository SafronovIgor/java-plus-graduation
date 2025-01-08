package ru.practicum.admin.service;

import jakarta.validation.ConstraintViolationException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.HttpStatsClient;
import ru.practicum.admin.repository.AdminCategoryRepository;
import ru.practicum.admin.repository.AdminEventRepository;
import ru.practicum.dto.StatResponseDto;
import ru.practicum.dto.event.*;
import ru.practicum.dto.event.request.RequestCount;
import ru.practicum.dto.event.request.Status;
import ru.practicum.exception.ConflictException;
import ru.practicum.exception.NotFoundException;
import ru.practicum.model.Category;
import ru.practicum.model.Event;
import ru.practicum.util.EventSearchParams;
import ru.practicum.util.StatParams;
import ru.practicum.util.Statistical;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class AdminEventServiceImpl implements AdminEventService {
    private final AdminEventRepository adminEventRepository;
    private final AdminCategoryRepository categoryRepository;
    private final UpdateEventAdminRequestMapper updateEventAdminRequestMapper;
    private final EventFullDtoMapper eventFullDtoMapper;
    private final HttpStatsClient httpStatsClient;

    @Override
    public List<EventFullDto> getEvents(EventSearchParams eventSearchParams) {
        LocalDateTime rangeStart = eventSearchParams.rangeStart();
        LocalDateTime rangeEnd = eventSearchParams.rangeEnd();
        if (rangeStart != null && rangeEnd != null && rangeStart.isAfter(rangeEnd)) {
            throw new ConstraintViolationException("Range start is after range end", Set.of());
        }
        Pageable pageable = PageRequest.of(eventSearchParams.from(), eventSearchParams.size());

        List<EventFullDto> eventFullDtoListSorted = adminEventRepository.getEventsOrderedById(
                eventSearchParams.users(),
                eventSearchParams.states(),
                eventSearchParams.categories(),
                eventSearchParams.loci(),
                rangeStart,
                rangeEnd,
                pageable);
        if (eventFullDtoListSorted.isEmpty()) {
            return eventFullDtoListSorted;
        }

        StatParams statParams = Statistical.getParams(new ArrayList<>(eventFullDtoListSorted));
        log.info("parameters for getting views of events: {}", eventFullDtoListSorted);
        List<StatResponseDto> statResponseDtoList =
                httpStatsClient.getStats(statParams.start(), statParams.end(), statParams.uriList(), true);
        if (statResponseDtoList.isEmpty()) {
            return eventFullDtoListSorted;
        }
        Map<Long, Long> hitMap = statResponseDtoList
                .stream()
                .collect(Collectors.toMap(x -> Long.parseLong(x.getUri().split("/")[2]), StatResponseDto::getHits));
        for (EventFullDto eventFullDto : eventFullDtoListSorted) {
            Long eventId = eventFullDto.getId();
            eventFullDto.setViews(hitMap.getOrDefault(eventId, 0L));
        }
        return eventFullDtoListSorted;
    }

    @Transactional
    @Override
    public EventFullDto updateEvent(Long eventId, UpdateEventAdminRequest adminRequest) {
        Event event = adminEventRepository.findById(eventId).orElseThrow(
                () -> new NotFoundException("Event not found")
        );
        if (event.getState() != State.PENDING) {
            throw new ConflictException("cannot publish twice");
        }
        Long categoryId = adminRequest.getCategoryId();
        Category category;
        if (categoryId != null) {
            category =
                    categoryRepository.findById(categoryId).orElseThrow(() -> new NotFoundException("Category not found"));
        } else {
            category = event.getCategory();
        }
        log.info("updating event: {}", event);

        event = updateEventAdminRequestMapper.updateEvent(adminRequest, event,
                category);
        log.info("updated event: {}", event);
        log.info("saving...");
        switch (adminRequest.getStateAction()) {
            case null:
                break;
            case PUBLISH_EVENT:
                event.setState(State.PUBLISHED);
                event.setPublishedOn(LocalDateTime.now());
                break;
            case REJECT_EVENT:
                event.setState(State.CANCELED);
                log.info("event state changed to {}", event.getState());
                break;
        }
        event = adminEventRepository.save(event);

        RequestCount requestCount = adminEventRepository.getRequestCountByEventAndStatus(eventId, Status.CONFIRMED);
        EventFullDto eventFullDto = eventFullDtoMapper.toDto(event, requestCount.getConfirmedRequests(), 0L);

        StatParams statParams = Statistical.getParams(List.of(eventFullDto));
        log.info("parameters for getting views of event: {}", statParams);
        List<StatResponseDto> statResponseDtoList =
                httpStatsClient.getStats(statParams.start(), statParams.end(), statParams.uriList(), true);
        Long hits = 0L;
        if (!statResponseDtoList.isEmpty()) {
            hits = statResponseDtoList.getFirst().getHits();
        }
        eventFullDto.setViews(hits);
        return eventFullDto;
    }
}
