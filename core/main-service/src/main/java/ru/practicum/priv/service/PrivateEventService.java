package ru.practicum.priv.service;

import org.springframework.transaction.annotation.Transactional;
import ru.practicum.dto.event.EventFullDto;
import ru.practicum.dto.event.EventShortDto;
import ru.practicum.dto.event.NewEventDto;
import ru.practicum.dto.event.UpdateEventUserRequest;
import ru.practicum.dto.event.request.EventRequestStatusUpdateRequest;
import ru.practicum.dto.event.request.EventRequestStatusUpdateResult;
import ru.practicum.dto.event.request.ParticipationRequestDto;

import java.util.List;

public interface PrivateEventService {
    @Transactional
    EventFullDto addEvent(Long userId, NewEventDto newEventDto);

    @Transactional(readOnly = true)
    List<EventShortDto> getMyEvents(Long userId, Integer from, Integer size);

    @Transactional(readOnly = true)
    EventFullDto getMyEvent(Long userId, Long eventId);

    @Transactional
    EventFullDto updateMyEvent(Long userId, Long eventId, UpdateEventUserRequest updateEventUserRequest);

    List<ParticipationRequestDto> getMyEventRequests(Long userId, Long eventId);

    @Transactional
    EventRequestStatusUpdateResult updateMyEventRequests(Long userId, Long eventId,
                                                         EventRequestStatusUpdateRequest updateRequest);
}
