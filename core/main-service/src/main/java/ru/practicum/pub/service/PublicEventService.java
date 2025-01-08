package ru.practicum.pub.service;

import jakarta.servlet.http.HttpServletRequest;
import ru.practicum.dto.event.EventFullDto;
import ru.practicum.dto.event.EventShortDto;
import ru.practicum.dto.event.SortCriterium;

import java.time.LocalDateTime;
import java.util.List;

public interface PublicEventService {
    EventFullDto getEvent(Long id, HttpServletRequest request);

    List<EventShortDto> getEvents(String text, Long[] categories, Boolean paid, LocalDateTime rangeStart,
                                  LocalDateTime rangeEnd, Boolean onlyAvailable, SortCriterium sort,
                                  Integer from, Integer size, HttpServletRequest request);
}
