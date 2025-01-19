package ru.practicum.event.repository;

import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;
import ru.practicum.enums.State;
import ru.practicum.event.model.Event;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class EventSpecifications {

    public static Specification<Event> withTextFilter(String text) {
        return (root, query, builder) -> {
            if (text == null || text.isBlank()) {
                return null;
            }
            String lowerText = "%" + text.toLowerCase() + "%";
            return builder.or(
                    builder.like(builder.lower(root.get("annotation")), lowerText),
                    builder.like(builder.lower(root.get("description")), lowerText)
            );
        };
    }

    public static Specification<Event> withCategories(List<Long> categories) {
        return (root, query, builder) -> {
            if (categories == null || categories.isEmpty()) {
                return null;
            }
            return root.get("category").get("id").in(categories);
        };
    }

    public static Specification<Event> withPaid(Boolean paid) {
        return (root, query, builder) -> paid != null ? builder.equal(root.get("paid"), paid) : null;
    }

    public static Specification<Event> withDateRange(LocalDateTime rangeStart, LocalDateTime rangeEnd) {
        return (root, query, builder) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (rangeStart != null) {
                predicates.add(builder.greaterThanOrEqualTo(root.get("eventDate"), rangeStart));
            }
            if (rangeEnd != null) {
                predicates.add(builder.lessThanOrEqualTo(root.get("eventDate"), rangeEnd));
            }
            return predicates.isEmpty() ? null : builder.and(predicates.toArray(new Predicate[0]));
        };
    }

    public static Specification<Event> withOnlyAvailable(boolean onlyAvailable) {
        return (root, query, builder) -> onlyAvailable
                ? builder.or(
                builder.equal(root.get("participantLimit"), 0),
                builder.greaterThan(root.get("participantLimit"), root.get("confirmedRequests"))
        )
                : null;
    }

    public static Specification<Event> withUsers(List<Long> users) {
        return (root, query, builder) -> {
            if (users == null || users.isEmpty()) {
                return null;
            }
            return root.get("initiatorId").in(users);
        };
    }

    public static Specification<Event> withState(State state) {
        return (root, query, builder) -> state != null ? builder.equal(root.get("state"), state) : null;
    }
}