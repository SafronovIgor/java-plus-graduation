package ru.practicum.admin.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.practicum.admin.service.AdminEventService;
import ru.practicum.dto.event.EventFullDto;
import ru.practicum.dto.event.State;
import ru.practicum.dto.event.UpdateEventAdminRequest;
import ru.practicum.util.EventSearchParams;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Эндпоинт возвращает полную информацию обо всех событиях подходящих под переданные условия
 * В случае, если по заданным фильтрам не найдено ни одного события, возвращает пустой список
 */
@RestController
@RequestMapping("/admin/events")
@RequiredArgsConstructor
public class AdminEventController {
    private final AdminEventService adminEventService;

    @GetMapping
    public ResponseEntity<List<EventFullDto>> getEvents(@RequestParam(required = false) List<Long> users,
                                                        @RequestParam(required = false) List<State> states,
                                                        @RequestParam(required = false) List<Long> categories,
                                                        @RequestParam(required = false)
                                                        @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
                                                        LocalDateTime rangeStart,
                                                        @RequestParam(required = false)
                                                        @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
                                                        LocalDateTime rangeEnd,
                                                        @RequestParam(required = false) List<Long> loci,
                                                        @RequestParam(required = false, defaultValue = "0")
                                                        Integer from,
                                                        @RequestParam(required = false, defaultValue = "10")
                                                        Integer size) {
        EventSearchParams eventSearchParams =
                new EventSearchParams(users, states, categories, rangeStart, rangeEnd, loci, from, size);
        return new ResponseEntity<>(
                adminEventService.getEvents(eventSearchParams), HttpStatus.OK);
    }

    @PatchMapping("/{eventId}")
    public ResponseEntity<EventFullDto> updateEvent(@PathVariable Long eventId,
                                                    @RequestBody @Valid UpdateEventAdminRequest adminRequest) {
        return new ResponseEntity<>(adminEventService.updateEvent(eventId, adminRequest), HttpStatus.OK);
    }
}
