package ru.practicum.user.action.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;
import ru.practicum.ewm.stats.avro.UserActionAvro;
import ru.practicum.user.action.model.UserAction;

import java.util.List;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface UserActionMapper {
    List<UserAction> listUserActionAvroToListUserAction(List<UserActionAvro> userActionAvro);
}