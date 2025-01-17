package ru.practicum.pub.repository;

import org.springframework.data.domain.Pageable;
import ru.practicum.dto.event.EventShortDto;

import java.time.LocalDateTime;
import java.util.List;

public interface PublicEventShortDtoRepository {
    List<EventShortDto> getEvents(String text, Long[] categories, Boolean paid, LocalDateTime rangeStart,
                                  LocalDateTime rangeEnd, Boolean onlyAvailable,
                                  Pageable pageable);
}
