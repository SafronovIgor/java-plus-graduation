package ru.practicum.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.client.request.RequestClient;
import ru.practicum.dto.request.ParticipationRequestDto;
import ru.practicum.service.RequestService;

import java.util.List;
import java.util.Set;

@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping("/users/")
public class RequestController implements RequestClient {
    private final RequestService requestService;

    @Override
    @GetMapping("/{userId}/requests")
    public List<ParticipationRequestDto> findAllRequestsByUserId(@PathVariable Long userId) {
        return requestService.findAllRequestsByUserId(userId);
    }

    @Override
    @PostMapping("/{userId}/requests")
    @ResponseStatus(HttpStatus.CREATED)
    public ParticipationRequestDto addRequest(@PathVariable Long userId,
                                              @RequestParam Long eventId) {
        return requestService.addRequest(userId, eventId);
    }

    @Override
    @PatchMapping("/{userId}/requests/{requestId}/cancel")
    public ParticipationRequestDto cancelRequest(@PathVariable Long userId,
                                                 @PathVariable Long requestId) {
        return requestService.cancelRequest(userId, requestId);
    }

    @Override
    @GetMapping("/requests/events/{eventId}")
    public List<ParticipationRequestDto> findAllRequestsByEventId(@PathVariable Long eventId) {
        return requestService.findAllRequestsByEventId(eventId);
    }

    @Override
    @GetMapping("/requests/events/{eventId}/status")
    public List<ParticipationRequestDto> findAllRequestsByEventIdAndStatus(@PathVariable Long eventId,
                                                                           @RequestParam String status) {
        return requestService.findAllRequestsByEventIdAndStatus(eventId, status);
    }

    @Override
    @GetMapping("/requests")
    public List<ParticipationRequestDto> findAllRequestsByRequestsId(@RequestParam Set<Long> requestsId) {
        return requestService.findAllRequestsByRequestsId(requestsId);
    }

    @Override
    @PutMapping("/requests/status")
    public List<ParticipationRequestDto> updateRequest(@RequestParam Set<Long> requestsId,
                                                       @RequestParam String status) {
        return requestService.updateRequest(requestsId, status);
    }

    @Override
    @GetMapping("/requests/existence")
    public boolean findExistRequests(@RequestParam Long eventId,
                                     @RequestParam Long userId,
                                     @RequestParam String status) {
        return requestService.findExistRequests(eventId, userId, status);
    }
}
