package ru.practicum.dto.event.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;


@Data
@AllArgsConstructor
@NoArgsConstructor
public class EventRequestStatusUpdateResult {
    private Set<ParticipationRequestDto> confirmedRequests;
    private Set<ParticipationRequestDto> rejectedRequests;
}
