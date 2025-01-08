package ru.practicum.priv.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import ru.practicum.dto.event.request.ParticipationRequestDto;
import ru.practicum.dto.event.request.Status;
import ru.practicum.model.Request;

import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface RequestRepository extends JpaRepository<Request, Long> {

    @Query("""
            SELECT
                new ru.practicum.dto.event.request.ParticipationRequestDto(
                    r.id,
                    r.event.id,
                    r.created,
                    r.requester.id,
                    r.status
                )
            FROM
                Request r
            WHERE
                r.requester.id = :userId
            """)
    List<ParticipationRequestDto> findDtosByRequesterId(Long userId);

    @Query("""
            SELECT
                new ru.practicum.dto.event.request.ParticipationRequestDto(
                    r.id,
                    r.event.id,
                    r.created,
                    r.requester.id,
                    r.status
                )
            FROM
                Request r
            WHERE
                r.event.id = :eventId
            """)
    List<ParticipationRequestDto> findDtosByEventId(Long eventId);

    @Query("""
            SELECT
                new ru.practicum.dto.event.request.ParticipationRequestDto(
                    r.id,
                    r.event.id,
                    r.created,
                    r.requester.id,
                    r.status
                )
            FROM
                Request r
            WHERE
                r.id in (:requestIds)
            """)
    List<ParticipationRequestDto> findDtosByIds(Set<Long> requestIds);

    List<Request> findAllByEventId(Long eventId);

    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query("""
            update Request r set r.status = :status where r.id in (:ids)
            """)
    void updateAllByIds(Set<Long> ids, Status status);

    Optional<Request> findByIdAndRequesterId(Long requestId, Long userId);

    boolean existsByRequesterIdAndEventId(Long userId, Long eventId);

    long countByEventIdAndStatus(Long eventId, Status status);

}