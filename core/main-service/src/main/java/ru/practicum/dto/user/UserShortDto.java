package ru.practicum.dto.user;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO part of {@link ru.practicum.dto.event.EventShortDto} and {@link ru.practicum.dto.event.EventFullDto}
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserShortDto {
    private Long id;
    private String name;
}
