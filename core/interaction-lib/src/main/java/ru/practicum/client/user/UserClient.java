package ru.practicum.client.user;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.dto.user.UserDto;
import ru.practicum.dto.user.UserRequestDto;

import java.util.List;


@FeignClient(name = "user-server")
public interface UserClient {
    @GetMapping("/admin/users")
    @Validated
    List<UserDto> getAllUsers(@RequestParam(required = false) List<Long> ids,
                              @RequestParam(defaultValue = "0") @PositiveOrZero Integer from,
                              @RequestParam(defaultValue = "10") @Positive Integer size);

    @PostMapping("/admin/users")
    @ResponseStatus(HttpStatus.CREATED)
    UserDto createUser(@RequestBody @Valid UserRequestDto userRequestDto);

    @DeleteMapping("/admin/users/{userId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    void deleteUser(@PathVariable long userId);

    @GetMapping("/admin/users/{userId}/existence")
    boolean getUserExists(@PathVariable long userId);
}
