package ru.practicum.priv.controller;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.MultiValueMap;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.DataTransferConvention;
import ru.practicum.dto.event.EventFullDto;
import ru.practicum.dto.event.EventShortDto;
import ru.practicum.dto.event.NewEventDto;
import ru.practicum.dto.event.UpdateEventUserRequest;
import ru.practicum.dto.event.request.EventRequestStatusUpdateRequest;
import ru.practicum.dto.event.request.EventRequestStatusUpdateResult;
import ru.practicum.dto.event.request.ParticipationRequestDto;
import ru.practicum.priv.service.PrivateEventService;

import java.util.List;

@RestController
@RequestMapping("users/{userId}/events")
@RequiredArgsConstructor
@Validated
@Slf4j
public class PrivateEventController {
    private final PrivateEventService privateEventService;
    private final MultiValueMap<String, String> headers = new HttpHeaders();

    @GetMapping
    public ResponseEntity<List<EventShortDto>> getMyEvents(@PathVariable("userId") @Min(1) @NotNull Long userId,
                                                           @RequestParam(defaultValue = DataTransferConvention.FROM)
                                                           Integer from,
                                                           @RequestParam(defaultValue = DataTransferConvention.SIZE)
                                                           Integer size) {
        headers.set(HttpHeaders.CONTENT_TYPE, "application/json");
        return new ResponseEntity<>(privateEventService.getMyEvents(userId, from, size), headers, HttpStatus.OK);
    }

    @PostMapping
    public ResponseEntity<EventFullDto> addEvent(@PathVariable("userId") @Min(1) @NotNull Long userId,
                                                 @RequestBody @Valid NewEventDto newEventDto) {
        headers.set(HttpHeaders.CONTENT_TYPE, "application/json");
        log.info("Adding new event: {}", newEventDto);
        return new ResponseEntity<>(privateEventService.addEvent(userId, newEventDto), headers, HttpStatus.CREATED);
    }

    @GetMapping("/{eventId}")
    public ResponseEntity<EventFullDto> getMyEvent(@PathVariable @Min(1) @NotNull Long userId,
                                                   @PathVariable @Min(1) @NotNull Long eventId) {
        headers.set(HttpHeaders.CONTENT_TYPE, "application/json");
        return new ResponseEntity<>(privateEventService.getMyEvent(userId, eventId), headers, HttpStatus.OK);
    }

    @PatchMapping("/{eventId}")
    public ResponseEntity<EventFullDto> updateMyEvent(@PathVariable @Min(1) @NotNull Long userId,
                                                      @PathVariable @Min(1) @NotNull Long eventId,
                                                      @RequestBody @Valid
                                                      UpdateEventUserRequest updateEventUserRequest) {
        headers.set(HttpHeaders.CONTENT_TYPE, "application/json");
        return new ResponseEntity<>(privateEventService.updateMyEvent(userId, eventId, updateEventUserRequest), headers,
                HttpStatus.OK);
    }

    @GetMapping("/{eventId}/requests")
    public ResponseEntity<List<ParticipationRequestDto>> getMyEventRequests(@PathVariable @Min(1) @NotNull Long userId,
                                                                            @PathVariable @Min(1) @NotNull
                                                                            Long eventId) {
        headers.set(HttpHeaders.CONTENT_TYPE, "application/json");
        return new ResponseEntity<>(privateEventService.getMyEventRequests(userId, eventId), headers, HttpStatus.OK);
    }

    @PatchMapping("/{eventId}/requests")
    public ResponseEntity<EventRequestStatusUpdateResult> updateMyEventRequests(
            @PathVariable @Min(1) @NotNull Long userId,
            @PathVariable @Min(1) @NotNull Long eventId,
            @RequestBody @Valid
            EventRequestStatusUpdateRequest updateRequest) {
        headers.set(HttpHeaders.CONTENT_TYPE, "application/json");
        return new ResponseEntity<>(privateEventService.updateMyEventRequests(userId, eventId, updateRequest), headers,
                HttpStatus.OK);
    }
}
