package ru.practicum.dto.request;

import com.fasterxml.jackson.annotation.JsonFormat;
import ru.practicum.Constants;
import ru.practicum.enums.Status;

import java.time.LocalDateTime;

public record ParticipationRequestDto(Long id,
                                      @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = Constants.DATE_TIME_FORMAT)
                                      LocalDateTime created,
                                      Long event,
                                      Long requester,
                                      Status status) {
}