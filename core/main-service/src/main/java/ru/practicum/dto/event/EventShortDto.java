package ru.practicum.dto.event;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.practicum.dto.category.CategoryDto;
import ru.practicum.dto.user.UserShortDto;
import ru.practicum.json.LocalDateTimeDeserializer;
import ru.practicum.json.LocalDateTimeSerializer;

import java.time.LocalDateTime;

/**
 * DTO response of the {@link ru.practicum.priv.controller.PrivateEventController}
 * {@link ru.practicum.pub.controller.PublicEventController},
 * part of {@link ru.practicum.dto.compilation.CompilationDto}
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EventShortDto implements EventDto {
    private Long id;
    private String annotation;
    private CategoryDto category;
    private Long confirmedRequests;
    @JsonSerialize(using = LocalDateTimeSerializer.class)
    @JsonDeserialize(using = LocalDateTimeDeserializer.class)
    private LocalDateTime eventDate;
    @JsonSerialize(using = LocalDateTimeSerializer.class)
    @JsonDeserialize(using = LocalDateTimeDeserializer.class)
    private LocalDateTime createdOn;
    private UserShortDto initiator;
    private Boolean paid;
    private String title;
    private Long views;
}
