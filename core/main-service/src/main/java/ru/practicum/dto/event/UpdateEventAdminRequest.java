package ru.practicum.dto.event;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.practicum.json.LocalDateTimeDeserializer;
import ru.practicum.json.LocalDateTimeSerializer;
import ru.practicum.validation.LaterThan;

import java.time.LocalDateTime;

/**
 * DTO request to the {@link ru.practicum.admin.controller.AdminEventController}
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateEventAdminRequest {
    @NotBlank
    @Size(min = 20, max = 2000)
    private String annotation;
    @Min(1)
    @JsonProperty("category")
    private Long categoryId;
    @NotBlank
    @Size(min = 20, max = 7000)
    private String description;
    @LaterThan(2)
    @JsonSerialize(using = LocalDateTimeSerializer.class)
    @JsonDeserialize(using = LocalDateTimeDeserializer.class)
    private LocalDateTime eventDate;
    @Valid
    private Location location;
    private Boolean paid;
    private Integer participantLimit;
    private Boolean requestModeration;
    private AdminStateAction stateAction;
    @NotBlank
    @Size(min = 3, max = 120)
    private String title;
}
