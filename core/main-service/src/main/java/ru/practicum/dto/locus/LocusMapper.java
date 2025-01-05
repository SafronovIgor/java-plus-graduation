package ru.practicum.dto.locus;

import org.mapstruct.*;
import ru.practicum.model.Locus;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING,
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface LocusMapper {
    Locus toLocus(NewLocusDto newLocusDto);

    @Mapping(target = "id", ignore = true)
    Locus updateLocus(@MappingTarget Locus locus, LocusUpdateDto locusUpdateDto);
}
