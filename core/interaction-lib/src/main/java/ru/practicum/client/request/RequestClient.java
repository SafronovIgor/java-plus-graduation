package ru.practicum.client.request;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.practicum.dto.request.ParticipationRequestDto;

import java.util.List;
import java.util.Set;

@FeignClient(name = "request-server")
public interface RequestClient {
    @GetMapping("/users/{userId}/requests")
    List<ParticipationRequestDto> findAllRequestsByUserId(@PathVariable Long userId);

    @PostMapping("/users/{userId}/requests")
    @ResponseStatus(HttpStatus.CREATED)
    ParticipationRequestDto addRequest(@PathVariable Long userId,
                                       @RequestParam Long eventId);

    @PatchMapping("/users/{userId}/requests/{requestId}/cancel")
    ParticipationRequestDto cancelRequest(@PathVariable Long userId,
                                          @PathVariable Long requestId);

    @GetMapping("/users/requests/events/{eventId}")
    List<ParticipationRequestDto> findAllRequestsByEventId(@PathVariable Long eventId);

    @GetMapping("/users/requests/events/{eventId}/status")
    List<ParticipationRequestDto> findAllRequestsByEventIdAndStatus(@PathVariable Long eventId,
                                                                    @RequestParam String status);

    @GetMapping("/users/requests")
    List<ParticipationRequestDto> findAllRequestsByRequestsId(@RequestParam Set<Long> requestsId);

    @PutMapping("/users/requests/status")
    List<ParticipationRequestDto> updateRequest(@RequestParam Set<Long> requestsId,
                                                @RequestParam String status);

    @GetMapping("/users/requests/existence")
    boolean findExistRequests(@RequestParam Long eventId,
                              @RequestParam Long userId,
                              @RequestParam String status);
}
