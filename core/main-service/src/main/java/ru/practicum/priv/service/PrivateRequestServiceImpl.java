package ru.practicum.priv.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.dto.event.State;
import ru.practicum.dto.event.request.ParticipationRequestDto;
import ru.practicum.dto.event.request.RequestDtoMapper;
import ru.practicum.dto.event.request.Status;
import ru.practicum.exception.ConflictException;
import ru.practicum.exception.NotFoundException;
import ru.practicum.model.Request;
import ru.practicum.priv.repository.PrivateEventRepository;
import ru.practicum.priv.repository.PrivateUserRepository;
import ru.practicum.priv.repository.RequestRepository;

import java.time.LocalDateTime;
import java.util.List;

@Service
@Transactional
@RequiredArgsConstructor
public class PrivateRequestServiceImpl implements PrivateRequestService {
    private final RequestRepository requestRepository;
    private final PrivateEventRepository eventRepository;
    private final PrivateUserRepository userRepository;
    private final RequestDtoMapper requestMapper;

    @Override
    @Transactional(readOnly = true)
    public List<ParticipationRequestDto> getMyRequests(Long userId) {
        return requestRepository.findDtosByRequesterId(userId);
    }

    @Override
    public ParticipationRequestDto addMyRequest(Long userId, Long eventId) {
        var user = userRepository.findById(userId).orElseThrow(NotFoundException::new);
        var event = eventRepository.findById(eventId).orElseThrow(NotFoundException::new);
        if (userId.equals(event.getInitiator().getId())) {
            throw new ConflictException("""
                    the initiator of the event cannot add a participation request for their own event.
                    """);
        }
        if (event.getState() != State.PUBLISHED) {
            throw new ConflictException("cannot participate in an unpublished event.");
        }
        if (requestRepository.existsByRequesterIdAndEventId(userId, eventId)) {
            throw new ConflictException("request already exists");
        }
        long confirmedAmount = requestRepository.countByEventIdAndStatus(eventId, Status.CONFIRMED);
        int participantLimit = event.getParticipantLimit();
        if (participantLimit > 0 && confirmedAmount >= participantLimit) {
            throw new ConflictException("participant limit exceeded");
        }
        Status status = Status.PENDING;
        if (!event.getRequestModeration() || participantLimit == 0) {
            status = Status.CONFIRMED;
        }
        var request = new Request(null, LocalDateTime.now(), user, event, status);

        return requestMapper.toParticipationRequestDto(requestRepository.save(request));
    }

    @Override
    public ParticipationRequestDto cancelMyRequest(Long userId, Long requestId) {
        var request = requestRepository.findByIdAndRequesterId(requestId, userId).orElseThrow(
                () -> new NotFoundException(String.format("request with id %s", requestId)));
        request.setStatus(Status.CANCELED);
        return requestMapper.toParticipationRequestDto(request);
    }
}
