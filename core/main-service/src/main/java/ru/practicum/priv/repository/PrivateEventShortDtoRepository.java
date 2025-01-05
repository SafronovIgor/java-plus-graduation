package ru.practicum.priv.repository;

import org.springframework.data.domain.Pageable;
import ru.practicum.dto.event.EventShortDto;

import java.util.List;

public interface PrivateEventShortDtoRepository {
    List<EventShortDto> getEvents(Long userId,
                                  Pageable pageable);
}
