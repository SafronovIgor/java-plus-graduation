package ru.practicum.pub.repository;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Tuple;
import jakarta.persistence.TypedQuery;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Root;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import ru.practicum.HttpStatsClient;
import ru.practicum.dto.StatResponseDto;
import ru.practicum.dto.event.EventShortDto;
import ru.practicum.dto.event.request.Status;
import ru.practicum.model.Compilation;
import ru.practicum.model.Request;
import ru.practicum.util.StatParams;
import ru.practicum.util.Statistical;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
public class CompilationDtoRepositoryImpl implements CompilationDtoRepository {
    @PersistenceContext
    private EntityManager em;

    public List<Compilation> findAllCompilations(Boolean pinned, Pageable pageable) {
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<Compilation> cq = cb.createQuery(Compilation.class);
        Root<Compilation> root = cq.from(Compilation.class);
        root.fetch("events", JoinType.LEFT);
        if (pinned != null) {
            cq.where(cb.equal(root.get("pinned"), pinned));
        }

        TypedQuery<Compilation> query = em.createQuery(cq);
        int pageSize = pageable.getPageSize();
        query.setFirstResult(pageable.getPageNumber() * pageSize);
        query.setMaxResults(pageSize);

        return query.getResultList();
    }

    @Override
    public void populateEventShortDtos(Set<EventShortDto> eventShortDtos,
                                       HttpStatsClient httpStatsClient) {
        // sort by EventShortDtos by id and put into map
        Map<Long, EventShortDto> sortedDtoMap =
                eventShortDtos.stream().sorted(Comparator.comparingLong(EventShortDto::getId)).collect(
                        Collectors.toMap(EventShortDto::getId, Function.identity(), (x, y) -> {
                            throw new RuntimeException("Duplicate key");
                        }, LinkedHashMap::new));
        log.info("Populated EventShortDtos: {}", sortedDtoMap);

        // get confirmed request amounts for each id in the map sorted by id
        Map<Long, Long> confReqMap = getConfReqMapSortedById(sortedDtoMap.keySet());
        log.info("Populated confReqList: {}", confReqMap);

        // get map of hits for each event id (no entries for events without views)
        StatParams statParams = Statistical.getParams(new ArrayList<>(eventShortDtos));
        List<StatResponseDto> statResponseDtoList =
                httpStatsClient.getStats(statParams.start(), statParams.end(), statParams.uriList(), true);
        Map<Long, Long> hitMap = statResponseDtoList
                .stream()
                .collect(Collectors.toMap(x -> Long.parseLong(x.getUri().split("/")[2]), StatResponseDto::getHits));
        log.info("Populated hitMap: {}", hitMap);

        //populate EventShortDtos with confirmed request amounts and views
        for (EventShortDto eventShortDto : sortedDtoMap.values()) {
            Long eventId = eventShortDto.getId();
            eventShortDto.setConfirmedRequests(confReqMap.getOrDefault(eventId, 0L));
            eventShortDto.setViews(hitMap.getOrDefault(eventId, 0L));
        }
    }

    private Map<Long, Long> getConfReqMapSortedById(Set<Long> sortedDtoKeySet) {
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<Tuple> cq = cb.createQuery(Tuple.class);
        Root<Request> root = cq.from(Request.class);
        cq.multiselect(root.get("event").get("id"), cb.count(root));
        cq.where(root.get("event").get("id").in(sortedDtoKeySet), cb.equal(root.get("status"), Status.CONFIRMED));
        cq.groupBy(root.get("event").get("id"));
        cq.orderBy(cb.asc(root.get("event").get("id")));
        List<Tuple> tupleList = em.createQuery(cq).getResultList();
        return tupleList.stream()
                .collect(Collectors.toMap(tuple -> tuple.get(0, Long.class), tuple -> tuple.get(1, Long.class)));
    }


}
