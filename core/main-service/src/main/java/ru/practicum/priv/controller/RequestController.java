package ru.practicum.priv.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.practicum.dto.event.request.ParticipationRequestDto;
import ru.practicum.priv.service.PrivateRequestServiceImpl;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/users/{userId}/requests")
@RequiredArgsConstructor
public class RequestController {

    private final PrivateRequestServiceImpl requestService;

    @GetMapping
    public ResponseEntity<List<ParticipationRequestDto>> getMyRequests(@PathVariable Long userId) {
        log.atInfo()
                .addArgument(userId)
                .log("Received request to get participation requests for userId: {}");
        return new ResponseEntity<>(requestService.getMyRequests(userId), HttpStatus.OK);
    }

    @PostMapping
    public ResponseEntity<ParticipationRequestDto> addMyRequest(@PathVariable Long userId, @RequestParam Long eventId) {
        log.atInfo()
                .addArgument(userId)
                .addArgument(eventId)
                .log("Received request to add participation request with userId: {} and eventId: {}");
        return new ResponseEntity<>(requestService.addMyRequest(userId, eventId), HttpStatus.CREATED);
    }

    @PatchMapping("/{requestId}/cancel")
    public ResponseEntity<ParticipationRequestDto> cancelMyRequest(@PathVariable Long userId,
                                                                   @PathVariable Long requestId) {
        log.atInfo()
                .addArgument(userId)
                .addArgument(requestId)
                .log("Received request to cancel participation request with userId: {} and requestId: {}");
        return new ResponseEntity<>(requestService.cancelMyRequest(userId, requestId), HttpStatus.OK);
    }
}
