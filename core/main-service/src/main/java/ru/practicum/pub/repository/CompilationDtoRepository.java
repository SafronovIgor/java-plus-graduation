package ru.practicum.pub.repository;

import org.springframework.data.domain.Pageable;
import ru.practicum.HttpStatsClient;
import ru.practicum.dto.event.EventShortDto;
import ru.practicum.model.Compilation;

import java.util.List;
import java.util.Set;

public interface CompilationDtoRepository {

    void populateEventShortDtos(Set<EventShortDto> eventShortDtos,
                                HttpStatsClient httpStatsClient);

    List<Compilation> findAllCompilations(Boolean pinned, Pageable pageable);
}

