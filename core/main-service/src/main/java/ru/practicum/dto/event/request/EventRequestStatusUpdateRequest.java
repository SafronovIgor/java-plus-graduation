package ru.practicum.dto.event.request;

import lombok.Data;

import java.util.Set;


@Data
public class EventRequestStatusUpdateRequest {
    private Set<Long> requestIds;
    private Status status;
}
