package ru.practicum.dto.compilation;

import org.mapstruct.*;
import ru.practicum.dto.event.EventShortDto;
import ru.practicum.model.Compilation;
import ru.practicum.model.Event;

import java.util.List;
import java.util.Set;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING,
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface CompilationDtoMapper {
    CompilationDto toCompilationDto(Compilation compilation);

    EventShortDto toEventShortDto(Event event);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "events", source = "events")
    Compilation updateCompilation(UpdateCompilationRequest updateCompilationRequest,
                                  @MappingTarget Compilation compilation, Set<Event> events);

    List<CompilationDto> toCompilationDtoList(List<Compilation> compilations);

    List<EventShortDto> toEventShortDtoList(List<Event> events);
}
