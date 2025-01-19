package ru.practicum.event.service;

import jakarta.servlet.http.HttpServletRequest;
import ru.practicum.dto.event.*;
import ru.practicum.dto.request.EventRequestStatusUpdateRequestDto;
import ru.practicum.dto.request.EventRequestStatusUpdateResultDto;
import ru.practicum.dto.request.ParticipationRequestDto;
import ru.practicum.enums.EventPublicSort;
import ru.practicum.enums.State;

import java.time.LocalDateTime;
import java.util.List;

public interface EventService {
    EventFullDto addEvent(NewEventDto newEventDto, long userId);

    EventFullDto findEventByUserIdAndEventId(long userId, long eventId, HttpServletRequest request);

    List<EventShortDto> findEventsByUser(long userId, int from, int size, HttpServletRequest request);

    EventFullDto updateEvent(UpdateEventUserRequest updateEventUserRequest, long userId, long eventId);

    List<ParticipationRequestDto> findRequestByEventId(long userId, long eventId);

    EventRequestStatusUpdateResultDto updateRequestByEventId(EventRequestStatusUpdateRequestDto updateRequest,
                                                             long userId,
                                                             long eventId);

    List<EventShortDto> findAllPublicEvents(String text, List<Long> categories, Boolean paid,
                                            LocalDateTime rangeStart, LocalDateTime rangeEnd, boolean onlyAvailable,
                                            EventPublicSort sort, int from, int size, HttpServletRequest request);

    EventFullDto findPublicEventById(long id, HttpServletRequest request);

    List<EventFullDto> findAllAdminEvents(List<Long> users, State state, List<Long> categories, LocalDateTime rangeStart,
                                          LocalDateTime rangeEnd, int from, int size, HttpServletRequest request);

    EventFullDto updateEventAdmin(UpdateEventAdminRequest updateEventAdminRequest, long eventId);

    boolean findExistEventByEventIdAndInitiatorId(Long eventId, Long initiatorId);

    EventFullDto findEventById(Long eventId);

    boolean findExistEventByEventId(Long eventId);
}
