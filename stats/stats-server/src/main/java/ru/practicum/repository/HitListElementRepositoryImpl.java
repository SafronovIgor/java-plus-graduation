package ru.practicum.repository;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.criteria.*;
import ru.practicum.dto.StatResponseDto;
import ru.practicum.model.ServiceHit;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public class HitListElementRepositoryImpl implements HitListElementRepository {
    @PersistenceContext
    private EntityManager em;

    @Override
    public List<StatResponseDto> getHitListElementDtos(LocalDateTime start,
                                                       LocalDateTime end,
                                                       String[] uris,
                                                       boolean unique) {
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<StatResponseDto> cq = cb.createQuery(StatResponseDto.class);
        Root<ServiceHit> root = cq.from(ServiceHit.class);
        List<Predicate> predicates = new ArrayList<>();
        Predicate datePredicate = cb.between(root.get("created"), start, end);
        predicates.add(datePredicate);

        if (uris != null && uris.length > 0 && !Objects.equals(uris[0], "/events")) {
            Predicate uriPredicate = root.get("uri").in(Arrays.asList(uris));
            predicates.add(uriPredicate);
        }

        Expression<Long> hitCountExpression = unique
                ? cb.countDistinct(root.get("ip"))
                : cb.count(root.get("ip"));

        cq.select(cb.construct(
                StatResponseDto.class,
                root.get("app"),
                root.get("uri"),
                hitCountExpression
        ));

        cq.where(predicates.toArray(new Predicate[0]))
                .groupBy(root.get("app"), root.get("uri"))
                .orderBy(cb.desc(hitCountExpression));

        return em.createQuery(cq).getResultList();
    }
}
