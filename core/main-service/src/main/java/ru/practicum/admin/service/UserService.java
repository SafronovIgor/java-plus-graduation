package ru.practicum.admin.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.admin.repository.AdminUserRepository;
import ru.practicum.dto.user.NewUserRequest;
import ru.practicum.dto.user.NewUserRequestMapper;
import ru.practicum.dto.user.UserDto;
import ru.practicum.dto.user.UserDtoMapper;
import ru.practicum.exception.ConflictException;
import ru.practicum.exception.NotFoundException;
import ru.practicum.model.User;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserService {
    private final AdminUserRepository adminUserRepository;
    private final NewUserRequestMapper newUserRequestMapper;
    private final UserDtoMapper userDtoMapper;

    public List<UserDto> getUsers(List<Long> ids, Integer page, Integer size) {
        Pageable pageRequest = PageRequest.of(page, size);
        if (ids == null || ids.isEmpty()) {
            return adminUserRepository.findAll(pageRequest).stream().map(userDtoMapper::toUserDto).toList();
        }
        return adminUserRepository.findUserDtosByIds(ids, pageRequest).getContent();
    }

    @Transactional
    public UserDto addUser(NewUserRequest newUserRequest) {
        String email = newUserRequest.getEmail();
        if (adminUserRepository.existsByEmail(email)) {
            throw new ConflictException("Email already exists");
        }
        User saved = adminUserRepository.save(newUserRequestMapper.toUser(newUserRequest));
        return userDtoMapper.toUserDto(saved);
    }

    public void deleteUser(Long userId) {
        if (!adminUserRepository.existsById(userId)) {
            throw new NotFoundException("User not found");
        }
        adminUserRepository.deleteById(userId);
    }
}
