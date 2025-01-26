package ru.practicum.event.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.Constants;
import ru.practicum.dto.event.EventFullDto;
import ru.practicum.dto.event.EventShortDto;
import ru.practicum.enums.EventPublicSort;
import ru.practicum.event.service.EventService;

import java.time.LocalDateTime;
import java.util.List;

@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping("/events")
public class EventPublicController {
    private final EventService eventService;

    @GetMapping
    public List<EventShortDto> findAllPublicEvents(@RequestParam(required = false) String text,
                                                   @RequestParam(required = false) List<Long> categories,
                                                   @RequestParam(required = false) Boolean paid,

                                                   @RequestParam(required = false)
                                                   @DateTimeFormat(pattern = Constants.DATE_TIME_FORMAT)
                                                   LocalDateTime rangeStart,

                                                   @RequestParam(required = false)
                                                   @DateTimeFormat(pattern = Constants.DATE_TIME_FORMAT)
                                                   LocalDateTime rangeEnd,

                                                   @RequestParam(defaultValue = "false") Boolean onlyAvailable,
                                                   @RequestParam(defaultValue = "EVENT_DATE") EventPublicSort sort,
                                                   @RequestParam(defaultValue = "0") @PositiveOrZero Integer from,
                                                   @RequestParam(defaultValue = "10") @Positive Integer size,
                                                   HttpServletRequest request) {
        return eventService.findAllPublicEvents(text, categories, paid,
                rangeStart, rangeEnd, onlyAvailable, sort, from, size, request);
    }

    @GetMapping("/{eventId}")
    public EventFullDto findPublicEventById(@PathVariable Long eventId, HttpServletRequest request) {
        return eventService.findPublicEventById(eventId, request);
    }
}