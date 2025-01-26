package ru.practicum.dto.user;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record UserShortDto(@NotNull
                           Long id,

                           @NotBlank
                           String name) {
}