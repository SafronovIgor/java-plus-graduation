package ru.practicum.client.event;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;
import ru.practicum.Constants;
import ru.practicum.dto.event.EventFullDto;
import ru.practicum.dto.event.UpdateEventAdminRequest;
import ru.practicum.enums.State;

import java.time.LocalDateTime;
import java.util.List;

@FeignClient(name = "event-server")
public interface EventClient {
    @GetMapping("/admin/events")
    List<EventFullDto> findAllAdminEvents(@RequestParam(required = false) List<Long> users,
                                          @RequestParam(required = false) State state,
                                          @RequestParam(required = false) List<Long> categories,
                                          @RequestParam(required = false)
                                          @DateTimeFormat(pattern = Constants.DATE_TIME_FORMAT)
                                          LocalDateTime rangeStart,
                                          @RequestParam(required = false)
                                          @DateTimeFormat(pattern = Constants.DATE_TIME_FORMAT)
                                          LocalDateTime rangeEnd,
                                          @RequestParam(defaultValue = "0") @PositiveOrZero int from,
                                          @RequestParam(defaultValue = "10") @Positive int size,
                                          HttpServletRequest request);

    @PatchMapping("/admin/events/{eventId}")
    EventFullDto updateEventAdmin(@RequestBody @Valid UpdateEventAdminRequest updateEventAdminRequest,
                                  @PathVariable long eventId);

    @GetMapping("admin/events/{eventId}/existence/{initiatorId}")
    boolean findExistEventByEventIdAndInitiatorId(@PathVariable @Positive Long eventId,
                                                  @PathVariable @Positive Long initiatorId);

    @GetMapping("/admin/events/{eventId}/existence")
    boolean findExistEventByEventId(@PathVariable @Positive Long eventId);

    @GetMapping("/admin/events/{eventId}")
    EventFullDto findEventById(@PathVariable Long eventId);
}
