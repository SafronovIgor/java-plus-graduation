package ru.practicum.admin.service;

import ru.practicum.dto.user.NewUserRequest;
import ru.practicum.dto.user.UserDto;

import java.util.List;

public interface UserService {
    List<UserDto> getUsers(List<Long> ids, Integer page, Integer size);

    UserDto addUser(NewUserRequest newUserRequest);

    void deleteUser(Long userId);
}
