package ru.practicum.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import ru.practicum.dto.user.UserDto;
import ru.practicum.dto.user.UserRequestDto;
import ru.practicum.exception.IntegrityViolationException;
import ru.practicum.exception.NotFoundException;
import ru.practicum.mapper.UserMapper;
import ru.practicum.model.User;
import ru.practicum.repository.UserRepository;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;
    private final UserMapper userMapper;

    @Override
    public List<UserDto> getAllUsers(List<Long> ids, int from, int size) {
        log.info("Fetching users with params: ids = {}, from = {}, size = {}", ids, from, size);
        PageRequest pageRequest = createPageRequest(from, size);
        List<User> users = fetchUsers(ids, pageRequest);
        List<UserDto> userDtos = mapUsersToDtos(users);
        log.info("Successfully fetched {} users", userDtos.size());
        return userDtos;
    }

    @Override
    @Transactional
    public UserDto createUser(UserRequestDto requestDto) {
        log.info("Creating user with data: {}", requestDto);
        User user = mapToUser(requestDto);
        verifyEmailNotExists(user.getEmail());
        userRepository.save(user);
        UserDto userDto = mapToUserDto(user);
        log.info("User successfully created: {}", userDto);
        return userDto;
    }

    @Override
    @Transactional
    public void deleteUser(long userId) {
        log.info("Deleting user with ID: {}", userId);
        verifyUserExists(userId);
        userRepository.deleteById(userId);
        log.info("User with ID: {} successfully deleted", userId);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean getUserExists(long userId) {
        log.info("Checking if user exists with ID: {}", userId);
        boolean exists = userRepository.existsById(userId);
        log.info("User existence check for ID: {} returned: {}", userId, exists);
        return exists;
    }

    private PageRequest createPageRequest(int from, int size) {
        log.debug("Creating PageRequest with from = {}, size = {}", from, size);
        return PageRequest.of(from, size, Sort.by(Sort.Direction.ASC, "id"));
    }

    private List<User> fetchUsers(List<Long> ids, PageRequest pageRequest) {
        log.debug("Fetching users with IDs: {} and PageRequest: {}", ids, pageRequest);
        if (CollectionUtils.isEmpty(ids)) {
            return userRepository.findAll(pageRequest).getContent();
        } else {
            return userRepository.findAllByIdIn(ids, pageRequest).getContent();
        }
    }

    private List<UserDto> mapUsersToDtos(List<User> users) {
        log.debug("Mapping {} users to DTOs", users.size());
        return userMapper.listUserToListUserDto(users);
    }

    private User mapToUser(UserRequestDto requestDto) {
        log.debug("Mapping UserRequestDto to User entity: {}", requestDto);
        return userMapper.userRequestDtoToUser(requestDto);
    }

    private void verifyEmailNotExists(String email) {
        log.debug("Checking if email already exists: {}", email);
        userRepository.findUserByEmail(email).ifPresent(u -> {
            log.error("Email already exists: {}", email);
            throw new IntegrityViolationException("User with email " + u.getEmail() + " already exists");
        });
        log.debug("Email does not exist: {}", email);
    }

    private UserDto mapToUserDto(User user) {
        log.debug("Mapping User entity to UserDto: {}", user);
        return userMapper.userToUserDto(user);
    }

    private void verifyUserExists(long userId) {
        log.debug("Verifying if user exists with ID: {}", userId);
        userRepository.findById(userId).orElseThrow(() -> {
            log.error("User not found with ID: {}", userId);
            return new NotFoundException("User with id = " + userId + " not found");
        });
        log.debug("User exists with ID: {}", userId);
    }
}