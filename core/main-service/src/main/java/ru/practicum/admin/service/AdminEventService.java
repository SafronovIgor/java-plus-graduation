package ru.practicum.admin.service;

import ru.practicum.dto.event.EventFullDto;
import ru.practicum.dto.event.UpdateEventAdminRequest;
import ru.practicum.util.EventSearchParams;

import java.util.List;

public interface AdminEventService {
    List<EventFullDto> getEvents(EventSearchParams eventSearchParams);

    EventFullDto updateEvent(Long eventId, UpdateEventAdminRequest adminRequest);
}
