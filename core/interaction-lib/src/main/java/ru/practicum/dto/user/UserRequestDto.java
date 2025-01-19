package ru.practicum.dto.user;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import org.hibernate.validator.constraints.Length;

public record UserRequestDto(@NotBlank
                             @Length(min = 6, max = 254)
                             @Email
                             String email,

                             @NotBlank
                             @Length(min = 2, max = 250)
                             String name) {
}