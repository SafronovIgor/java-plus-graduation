package ru.practicum.dto.event.request;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import ru.practicum.model.Request;

import java.util.Collection;
import java.util.Set;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface RequestDtoMapper {
    @Mapping(source = "requester.id", target = "requester")
    @Mapping(source = "event.id", target = "eventId")
    ParticipationRequestDto toParticipationRequestDto(Request request);

    @Mapping(source = "requester.id", target = "requester")
    @Mapping(source = "event.id", target = "eventId")
    Set<ParticipationRequestDto> toParticipationRequestDtos(Collection<Request> requests);
}
