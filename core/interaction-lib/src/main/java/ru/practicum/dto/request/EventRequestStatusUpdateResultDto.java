package ru.practicum.dto.request;

import java.util.List;

public record EventRequestStatusUpdateResultDto(List<ParticipationRequestDto> confirmedRequests,
                                                List<ParticipationRequestDto> rejectedRequests) {
}