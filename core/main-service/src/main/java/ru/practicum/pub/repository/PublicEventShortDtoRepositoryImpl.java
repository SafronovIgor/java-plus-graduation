package ru.practicum.pub.repository;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.criteria.*;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;
import ru.practicum.dto.category.CategoryDto;
import ru.practicum.dto.event.EventShortDto;
import ru.practicum.dto.event.request.Status;
import ru.practicum.dto.user.UserShortDto;
import ru.practicum.model.Category;
import ru.practicum.model.Event;
import ru.practicum.model.Request;
import ru.practicum.model.User;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Repository
public class PublicEventShortDtoRepositoryImpl implements PublicEventShortDtoRepository {

    @PersistenceContext
    private EntityManager em;

    @Override
    public List<EventShortDto> getEvents(String text, Long[] categories, Boolean paid, LocalDateTime rangeStart,
                                         LocalDateTime rangeEnd, Boolean onlyAvailable,
                                         Pageable pageable) {
        Object[] params = new Object[]{text, categories, paid, rangeStart, rangeEnd, onlyAvailable};
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<EventShortDto> cq = cb.createQuery(EventShortDto.class);
        Root<Event> eventRoot = cq.from(Event.class);
        Join<Event, Category> categoryJoin = eventRoot.join("category");
        Join<Event, User> userJoin = eventRoot.join("initiator");
        Subquery<Long> subquery = cq.subquery(Long.class);
        Root<Request> subRoot = subquery.from(Request.class);
        subquery.select(cb.count(subRoot.get("id")));
        subquery.where(
                cb.equal(subRoot.get("event").get("id"), eventRoot.get("id")),
                cb.equal(subRoot.get("status"), Status.CONFIRMED)
        );
        cq.select(cb.construct(
                EventShortDto.class,
                eventRoot.get("id"),
                eventRoot.get("annotation"),
                cb.construct(
                        CategoryDto.class, categoryJoin.get("id"), categoryJoin.get("name")
                ),
                subquery.getSelection(),
                eventRoot.get("eventDate"),
                eventRoot.get("createdOn"),
                cb.construct(
                        UserShortDto.class, userJoin.get("id"), userJoin.get("name")
                ),
                eventRoot.get("paid"),
                eventRoot.get("title"),
                cb.nullLiteral(Long.class) // views will be filled later
        ));

        List<Predicate> predicates = getPredicates(cb, eventRoot, categoryJoin, params);
        if (!predicates.isEmpty()) {
            cq.where(cb.and(predicates.toArray(new Predicate[0])));
        }
        int pageSize = pageable.getPageSize();
        return em.createQuery(cq)
                .setFirstResult(pageable.getPageNumber() * pageSize)
                .setMaxResults(pageSize)
                .getResultList();
    }

    private List<Predicate> getPredicates(CriteriaBuilder cb, Root<Event> eventRoot, Join<Event, Category> categoryJoin,
                                          Object... args) {
        List<Predicate> predicates = new ArrayList<>();
        String text = (String) args[0];
        if (text != null && !text.isEmpty()) {
            String searchText = "%" + text.toLowerCase() + "%";
            Predicate textInAnnotation = cb.like(cb.lower(eventRoot.get("annotation")), searchText);
            Predicate textInDescription = cb.like(cb.lower(eventRoot.get("description")), searchText);
            predicates.add(cb.or(textInAnnotation, textInDescription));
        }
        Long[] categories = (Long[]) args[1];
        if (categories != null && categories.length > 0) {
            predicates.add(categoryJoin.get("id").in((Object[]) categories));
        }
        Boolean paid = (Boolean) args[2];
        if (paid != null) {
            predicates.add(cb.equal(eventRoot.get("paid"), paid));
        }
        LocalDateTime rangeStart = (LocalDateTime) args[3];
        LocalDateTime rangeEnd = (LocalDateTime) args[4];
        if (rangeStart != null && rangeEnd != null) {
            predicates.add(cb.between(eventRoot.get("eventDate"), rangeStart, rangeEnd));
        } else if (rangeStart != null) {
            predicates.add(cb.between(eventRoot.get("eventDate"), rangeStart, LocalDateTime.now()));
        } else {
            predicates.add(cb.greaterThan(eventRoot.get("eventDate"), LocalDateTime.now()));
        }
        Boolean onlyAvailable = (Boolean) args[5];
        if (Boolean.TRUE.equals(onlyAvailable)) {
            predicates.add(cb.greaterThan(eventRoot.get("participantLimit"), eventRoot.get("confirmedRequests")));
        }
        return predicates;
    }
}
