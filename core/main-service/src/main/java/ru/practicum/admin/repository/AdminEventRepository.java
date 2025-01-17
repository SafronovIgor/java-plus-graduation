package ru.practicum.admin.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import ru.practicum.dto.event.request.RequestCount;
import ru.practicum.dto.event.request.Status;
import ru.practicum.model.Event;

import java.util.Collection;
import java.util.Set;

public interface AdminEventRepository extends JpaRepository<Event, Long>, AdminEventFullDtoRepository {
    @Query("""
            select new ru.practicum.dto.event.request.RequestCount(count(r.id))
            from Request r
            where r.event.id=:eventId and r.status=:status
            """)
    RequestCount getRequestCountByEventAndStatus(Long eventId, Status status);

    Set<Event> findAllByIdIn(Collection<Long> id);

}
