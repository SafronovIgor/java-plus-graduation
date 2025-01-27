package ru.practicum.events.similarity.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.practicum.events.similarity.model.EventSimilarity;

import java.util.List;

public interface EventSimilarityRepository extends JpaRepository<EventSimilarity, Long> {

    @Query("""
            select
                es
            from
                EventSimilarity as es
            where
                (es.eventA = :targetEventId or es.eventB = :targetEventId)
                and (es.eventB not in :eventsId and es.eventA not in :eventsId)
            group by
                es.score
            order by
                es.score
            """)
    List<EventSimilarity> findEventSimilaritiesByEventsId(@Param("targetEventId") Long targetEventId,
                                                          @Param("eventsId") List<Long> eventsId);

    @Query("""
            select
                es
            from
                EventSimilarity as es
            where
                es.eventA in :eventsId
                and es.eventB not in :eventsId
            group by
                es.score
            order by
                es.score desc
            limit :limit
            """)
    List<EventSimilarity> findSimilarEvents(@Param("eventsId") List<Long> eventsId, @Param("limit") int limit);

    @Query("""
            select
                es
            from
                EventSimilarity as es
            where
                es.eventA = :eventId
            group by
                es.score
            order by
                es.score desc
            limit :limit
            """)
    List<EventSimilarity> findNearestNeighbors(@Param("eventId") Long eventId, @Param("limit") int limit);
}