package ru.practicum.event.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;
import ru.practicum.Constants;
import ru.practicum.client.event.EventClient;
import ru.practicum.dto.event.EventFullDto;
import ru.practicum.dto.event.UpdateEventAdminRequest;
import ru.practicum.enums.State;
import ru.practicum.event.service.EventService;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/admin/events")
public class EventAdminController implements EventClient {
    private final EventService eventService;

    @Override
    @GetMapping
    public List<EventFullDto> findAllAdminEvents(@RequestParam(required = false) List<Long> users,
                                                 @RequestParam(required = false) State states,
                                                 @RequestParam(required = false) List<Long> categories,

                                                 @RequestParam(required = false)
                                                 @DateTimeFormat(pattern = Constants.DATE_TIME_FORMAT)
                                                 LocalDateTime rangeStart,

                                                 @RequestParam(required = false)
                                                 @DateTimeFormat(pattern = Constants.DATE_TIME_FORMAT)
                                                 LocalDateTime rangeEnd,

                                                 @RequestParam(defaultValue = "0") @PositiveOrZero int from,
                                                 @RequestParam(defaultValue = "10") @Positive int size,
                                                 HttpServletRequest request) {
        return eventService.findAllAdminEvents(users, states, categories, rangeStart, rangeEnd, from, size, request);
    }

    @Override
    @PatchMapping("/{eventId}")
    public EventFullDto updateEventAdmin(@RequestBody @Valid UpdateEventAdminRequest updateEventAdminRequest,
                                         @PathVariable long eventId) {
        return eventService.updateEventAdmin(updateEventAdminRequest, eventId);
    }

    @Override
    @GetMapping("/{eventId}")
    public EventFullDto findEventById(@PathVariable Long eventId) {
        return eventService.findEventById(eventId);
    }

    @Override
    @GetMapping("/{eventId}/existence/{initiatorId}")
    public boolean findExistEventByEventIdAndInitiatorId(@PathVariable @Positive Long eventId,
                                                         @PathVariable @Positive Long initiatorId) {
        return eventService.findExistEventByEventIdAndInitiatorId(eventId, initiatorId);
    }

    @Override
    @GetMapping("/{eventId}/existence/")
    public boolean findExistEventByEventId(@PathVariable @Positive Long eventId) {
        return eventService.findExistEventByEventId(eventId);
    }
}