package ru.practicum.dto.user;

import org.mapstruct.Mapper;
import ru.practicum.model.User;

import static org.mapstruct.MappingConstants.ComponentModel.SPRING;

@Mapper(componentModel = SPRING)
public interface UserDtoMapper {
    UserDto toUserDto(User user);

    User toUser(UserDto userDto);
}
