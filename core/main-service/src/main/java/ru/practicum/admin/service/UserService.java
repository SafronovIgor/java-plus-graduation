package ru.practicum.admin.service;

import org.springframework.transaction.annotation.Transactional;
import ru.practicum.dto.user.NewUserRequest;
import ru.practicum.dto.user.UserDto;

import java.util.List;

public interface UserService {
    List<UserDto> getUsers(List<Long> ids, Integer page, Integer size);

    @Transactional
    UserDto addUser(NewUserRequest newUserRequest);

    @Transactional
    void deleteUser(Long userId);
}
