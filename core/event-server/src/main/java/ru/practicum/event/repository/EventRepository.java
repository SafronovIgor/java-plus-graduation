package ru.practicum.event.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import ru.practicum.enums.State;
import ru.practicum.event.model.Event;

import java.util.List;
import java.util.Optional;

public interface EventRepository extends JpaRepository<Event, Long>, JpaSpecificationExecutor<Event> {
    Optional<Event> findByIdAndState(Long id, State state);

    List<Event> findAllByIdIn(List<Long> ids);

    List<Event> findAllByCategoryId(Long categoryId);

    boolean existsByIdAndInitiatorId(Long id, Long initiatorId);

    Page<Event> findByInitiatorId(long userId, PageRequest pageRequest);
}