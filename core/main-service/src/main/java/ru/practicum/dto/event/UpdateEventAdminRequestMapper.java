package ru.practicum.dto.event;

import org.mapstruct.*;
import ru.practicum.model.Category;
import ru.practicum.model.Event;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING, nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface UpdateEventAdminRequestMapper {
    @Mapping(target = "category", source = "category")
    @Mapping(target = "id", ignore = true)
    Event updateEvent(UpdateEventAdminRequest updateEventAdminRequest, @MappingTarget Event event, Category category);
}
