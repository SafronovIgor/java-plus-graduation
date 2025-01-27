package ru.practicum.events.similarity.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.events.similarity.mapper.EventSimilarityMapper;
import ru.practicum.events.similarity.model.EventSimilarity;
import ru.practicum.events.similarity.repository.EventSimilarityRepository;
import ru.practicum.ewm.stats.avro.EventSimilarityAvro;
import ru.practicum.ewm.stats.proto.InteractionsCountRequestProto;
import ru.practicum.ewm.stats.proto.RecommendedEventProto;
import ru.practicum.ewm.stats.proto.SimilarEventsRequestProto;
import ru.practicum.ewm.stats.proto.UserPredictionsRequestProto;
import ru.practicum.user.action.model.ActionType;
import ru.practicum.user.action.model.UserAction;
import ru.practicum.user.action.repository.UserActionRepository;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static ru.practicum.Const.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class EventSimilarityServiceImpl implements EventSimilarityService {
    private final EventSimilarityRepository eventSimilarityRepository;
    private final UserActionRepository userActionRepository;
    private final EventSimilarityMapper eventSimilarityMapper;

    @Override
    @Transactional
    public void saveEventsSimilarity(List<EventSimilarityAvro> eventsSimilarityAvro) {
        List<EventSimilarity> eventsSimilarity = eventSimilarityMapper
                .listEventSimilarityAvroToListEventSimilarity(eventsSimilarityAvro);
        eventSimilarityRepository.saveAll(eventsSimilarity);
        log.info("Saved {} event similarities.", eventsSimilarity.size());
    }

    @Override
    public List<RecommendedEventProto> getSimilarEvents(SimilarEventsRequestProto request) {
        List<Long> eventsId = userActionRepository.findEventsIdByUserId(request.getUserId());
        eventsId.remove(request.getEventId());

        if (eventsId.isEmpty()) {
            log.warn("No events found for user {} to calculate similarity.", request.getUserId());
            return List.of();
        }

        List<EventSimilarity> similarities = eventSimilarityRepository
                .findEventSimilaritiesByEventsId(request.getEventId(), eventsId);

        return eventSimilarityMapper.listEventSimilarityToListRecommendedEventProto(similarities);
    }

    @Override
    public List<RecommendedEventProto> getInteractionsCount(InteractionsCountRequestProto request) {
        List<UserAction> userActions = userActionRepository.findUserActionByEventIdIn(request.getEventIdList());

        return request.getEventIdList().stream()
                .map(eventId -> {
                    double weight = userActions.stream()
                            .filter(action -> action.getEventId().equals(eventId))
                            .mapToDouble(action -> getWeight(action.getActionType()))
                            .sum();
                    return buildRecommendedEventProto(eventId, weight);
                })
                .toList();
    }

    @Override
    public List<RecommendedEventProto> getRecommendationsForUser(UserPredictionsRequestProto request) {
        List<Long> recentEventIds = userActionRepository
                .findEventsIdByUserIdOrderByActionDateDesc(request.getUserId(), request.getMaxResults());

        if (recentEventIds.isEmpty()) {
            log.warn("No recent events found for user {} to generate recommendations.", request.getUserId());
            return List.of();
        }

        List<EventSimilarity> similarEvents = eventSimilarityRepository
                .findSimilarEvents(recentEventIds, request.getMaxResults());
        List<Long> similarEventIds = similarEvents.stream()
                .map(EventSimilarity::getEventB)
                .toList();

        return similarEventIds.stream()
                .map(eventId -> calculateRatingNewEvent(eventId, recentEventIds, request))
                .toList();
    }

    private double getWeight(ActionType actionType) {
        return switch (actionType) {
            case VIEW -> VIEWING_RATIO;
            case REGISTER -> REGISTRATION_RATIO;
            case LIKE -> LIKE_RATIO;
            default -> 0.0;
        };
    }

    private double calculateEvaluationNewEvent(List<EventSimilarity> similarEvents,
                                               Map<Long, ActionType> eventsRating) {
        double sumWeightedEstimates = similarEvents.stream()
                .mapToDouble(event ->
                        event.getScore() * getWeight(eventsRating.getOrDefault(event.getEventB(), ActionType.UNKNOWN)))
                .sum();

        double sumSimilarityCoefficients = similarEvents.stream()
                .mapToDouble(EventSimilarity::getScore)
                .sum();

        return sumSimilarityCoefficients == 0 ? 0 : sumWeightedEstimates / sumSimilarityCoefficients;
    }

    private RecommendedEventProto calculateRatingNewEvent(Long eventId,
                                                          List<Long> userRecentEvents,
                                                          UserPredictionsRequestProto request) {
        List<EventSimilarity> nearestNeighbors = eventSimilarityRepository
                .findNearestNeighbors(eventId, request.getMaxResults());

        List<Long> userInteractionEventIds = nearestNeighbors.stream()
                .map(EventSimilarity::getEventB)
                .filter(userRecentEvents::contains)
                .toList();

        Map<Long, ActionType> eventsRating = userActionRepository
                .findUserActionByEventIdIn(userInteractionEventIds).stream()
                .collect(Collectors.toMap(UserAction::getEventId, UserAction::getActionType));

        double evaluation = calculateEvaluationNewEvent(nearestNeighbors, eventsRating);

        return buildRecommendedEventProto(eventId, evaluation);
    }

    private RecommendedEventProto buildRecommendedEventProto(Long eventId, Double score) {
        return RecommendedEventProto.newBuilder()
                .setEventId(eventId)
                .setScore(score)
                .build();
    }
}