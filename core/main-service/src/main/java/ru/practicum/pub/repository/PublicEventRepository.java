package ru.practicum.pub.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import ru.practicum.dto.event.State;
import ru.practicum.dto.event.request.RequestCount;
import ru.practicum.dto.event.request.Status;
import ru.practicum.model.Event;

import java.util.Optional;

public interface PublicEventRepository extends JpaRepository<Event, Long>, PublicEventShortDtoRepository {
    @Query("""
            select new ru.practicum.dto.event.request.RequestCount(count(r.id))
            from Request r
            where r.event.id=:eventId and r.status=:status
            """)
    RequestCount getRequestCountByEventAndStatus(Long eventId, Status status);

    Optional<Event> findByIdAndState(Long id, State state);
}