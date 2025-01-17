package ru.practicum.priv.service;

import ru.practicum.dto.event.request.ParticipationRequestDto;

import java.util.List;

public interface PrivateRequestService {

    List<ParticipationRequestDto> getMyRequests(Long userId);

    ParticipationRequestDto addMyRequest(Long userId, Long eventId);

    ParticipationRequestDto cancelMyRequest(Long userId, Long requestId);
}
