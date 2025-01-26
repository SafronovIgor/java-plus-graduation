package ru.practicum.user.actions.service;

import lombok.RequiredArgsConstructor;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.stereotype.Service;
import ru.practicum.ewm.stats.avro.ActionTypeAvro;
import ru.practicum.ewm.stats.avro.EventSimilarityAvro;
import ru.practicum.ewm.stats.avro.UserActionAvro;

import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static ru.practicum.WeightCoefficients.*;

@Service
@RequiredArgsConstructor
public class AggregatorServiceImpl implements AggregatorService {
    private final Map<Long, Double> sumWeightsUserAction = new HashMap<>();
    private final Map<Long, Map<Long, Double>> matrixWeightsUserAction = new HashMap<>();
    private final Map<Long, Map<Long, Double>> sumMinimumWeightsUserAction = new HashMap<>();

    @Override
    public List<EventSimilarityAvro> handle(ConsumerRecord<Void, UserActionAvro> record) {
        var userActionAvro = record.value();
        long eventId = userActionAvro.getEventId();
        long userId = userActionAvro.getUserId();
        ActionTypeAvro actionType = userActionAvro.getActionType();

        if (matrixWeightsUserAction.isEmpty()) {
            addNewUserActionWeight(eventId, userId, false, actionType);
            return List.of();
        }

        Map<Long, Double> userActionWeights = matrixWeightsUserAction.get(eventId);

        if (userActionWeights != null) {
            Double existingWeight = userActionWeights.get(userId);
            double actionWeight = getWeight(userActionAvro.getActionType());

            if (existingWeight == null || existingWeight < actionWeight) {
                return recalculateSimilarity(userActionAvro);
            }
        }

        addNewUserActionWeight(eventId, userId, true, actionType);
        return recalculateSimilarity(userActionAvro);
    }

    private void addNewUserActionWeight(long eventId, long userId, boolean isUserActive, ActionTypeAvro type) {
        var actionWeight = isUserActive ? ABSENCE_OF_ACTIONS_RATIO : getWeight(type);

        matrixWeightsUserAction.computeIfAbsent(eventId, k -> new HashMap<>()).put(userId, actionWeight);
        sumWeightsUserAction.put(eventId, actionWeight);
    }

    private double getWeight(ActionTypeAvro actionTypeAvro) {
        if (ActionTypeAvro.VIEW == actionTypeAvro) {
            return VIEWING_RATIO;
        } else if (ActionTypeAvro.REGISTER == actionTypeAvro) {
            return REGISTRATION_RATIO;
        } else if (ActionTypeAvro.LIKE == actionTypeAvro) {
            return LIKE_RATIO;
        }
        return 0.0;
    }

    private List<EventSimilarityAvro> recalculateSimilarity(UserActionAvro userActionAvro) {
        long userId = userActionAvro.getUserId();
        long eventId = userActionAvro.getEventId();
        ActionTypeAvro actionType = userActionAvro.getActionType();
        double currentActionWeight = getWeight(actionType);

        Map<Long, Double> userActionsWeightsByEventId =
                matrixWeightsUserAction.computeIfAbsent(eventId, k -> new HashMap<>());

        double previousActionWeight = userActionsWeightsByEventId.getOrDefault(userId, 0.0);

        List<Long> userInteractionEvents = getUserActionsByUserId(userId, eventId);

        if (userInteractionEvents.isEmpty()) {
            userActionsWeightsByEventId.put(userId, currentActionWeight);
            sumWeightsUserAction.put(eventId, currentActionWeight);
            return List.of();
        }

        List<EventSimilarityAvro> eventsSimilarityAvro = new ArrayList<>();

        for (Long event : userInteractionEvents) {
            long firstEventId = Math.min(event, eventId);
            long secondEventId = Math.max(event, eventId);

            Map<Long, Double> userActionWeights = matrixWeightsUserAction.getOrDefault(event, Map.of());

            double deltaContribution = Math.min(
                    currentActionWeight, userActionWeights.getOrDefault(userId, 0.0)
            ) - Math.min(
                    previousActionWeight, userActionWeights.getOrDefault(userId, 0.0)
            );
            double deltaOldNewWeight = currentActionWeight - previousActionWeight;

            if (deltaContribution != 0.0) {
                sumMinimumWeightsUserAction
                        .computeIfAbsent(firstEventId, k -> new HashMap<>())
                        .merge(secondEventId, deltaContribution, Double::sum);
            }

            double similarityScore = calculateSimilarity(eventId, firstEventId, secondEventId, deltaOldNewWeight);

            eventsSimilarityAvro.add(
                    EventSimilarityAvro.newBuilder()
                            .setEventA(firstEventId)
                            .setEventB(secondEventId)
                            .setScore(similarityScore)
                            .setTimestamp(Instant.now())
                            .build()
            );
        }

        userActionsWeightsByEventId.put(userId, currentActionWeight);

        return eventsSimilarityAvro;
    }

    private List<Long> getUserActionsByUserId(Long userId, Long eventId) {
        return matrixWeightsUserAction.entrySet()
                .stream()
                .filter(entry ->
                        !entry.getKey().equals(eventId) && entry.getValue().containsKey(userId)
                )
                .map(Map.Entry::getKey)
                .toList();
    }

    private Double calculateSimilarity(long eventId, long firstEventId, long secondEventId, double deltaOldNewWeight) {
        double newSumWeights = sumWeightsUserAction.merge(eventId, deltaOldNewWeight, Double::sum);

        double sumMin = sumMinimumWeightsUserAction
                .computeIfAbsent(firstEventId, e -> new HashMap<>())
                .getOrDefault(secondEventId, 0.0);

        double secondEventWeight = sumWeightsUserAction.getOrDefault(secondEventId, 0.0);

        return sumMin / (Math.sqrt(newSumWeights) + Math.sqrt(secondEventWeight));
    }
}

//todo: v1
//private List<EventSimilarityAvro> recalculateSimilarity(UserActionAvro userActionAvro) {
//        List<EventSimilarityAvro> eventsSimilarityAvro = new ArrayList<>();
//        long userId = userActionAvro.getUserId();
//        long eventId = userActionAvro.getEventId();
//        ActionTypeAvro actionType = userActionAvro.getActionType();
//        Map<Long, Double> userActionsWeightsByEventId = matrixWeightsUserAction.get(eventId);
//
//        List<Long> userInteractionEvents = getUserActionsByUserId(userId, eventId);
//
//        if (userInteractionEvents.isEmpty()) {
//            double weight = getWeight(actionType);
//            userActionsWeightsByEventId.put(userId, weight);
//            sumWeightsUserAction.put(eventId, weight);
//            return List.of();
//        }
//
//
//        for (Long event : userInteractionEvents) {
//            long firstEventId = Math.min(event, eventId);
//            long secondEventId = Math.max(event, eventId);
//            Map<Long, Double> userActionWeights = matrixWeightsUserAction.get(event);
//            double deltaContribution = Math.min(
//                    getWeight(actionType), userActionWeights.get(userId)
//            ) - Math.min(
//                    userActionsWeightsByEventId.getOrDefault(userId, 0.0), userActionWeights.get(userId)
//            );
//            double deltaOldNewWeight =
//                    getWeight(actionType) - userActionsWeightsByEventId.getOrDefault(userId, 0.0);
//
//            if (deltaContribution != 0.0) {
//                if (!sumMinimumWeightsUserAction.containsKey(firstEventId)) {
//                    calculateSumMin(firstEventId, secondEventId);
//                }
//
//                Map<Long, Double> sumMinimumWeightsEvent = sumMinimumWeightsUserAction.get(firstEventId);
//                sumMinimumWeightsEvent
//                        .replace(secondEventId, sumMinimumWeightsEvent.get(secondEventId) + deltaContribution);
//            }
//
//            double summarily = calculateSimilarity(eventId, firstEventId, secondEventId, deltaOldNewWeight);
//            eventsSimilarityAvro.add(
//                    EventSimilarityAvro.newBuilder()
//                            .setEventA(firstEventId)
//                            .setEventB(secondEventId)
//                            .setScore(summarily)
//                            .setTimestamp(Instant.now())
//                            .build()
//            );
//        }
//
//        if (userActionsWeightsByEventId.containsKey(userId)) {
//            userActionsWeightsByEventId.replace(userId, getWeight(actionType));
//        } else {
//            userActionsWeightsByEventId.put(eventId, getWeight(actionType));
//        }
//
//        return eventsSimilarityAvro;
//    }

//private void calculateSumMin(long firstEventId, long secondEventId) {
//        Map<Long, Double> userActionWeightsByFirstEventId = matrixWeightsUserAction
//                .getOrDefault(firstEventId, Map.of());
//
//        Map<Long, Double> userActionWeightsBySecondEventId = matrixWeightsUserAction
//                .getOrDefault(secondEventId, Map.of());
//
//        long maxUserId = Stream.concat(
//                        userActionWeightsByFirstEventId.keySet().stream(),
//                        userActionWeightsBySecondEventId.keySet().stream()
//                )
//                .max(Long::compareTo)
//                .orElse(0L);
//
//        double minSum = Stream.iterate(0L, i -> i + 1)
//                .limit(maxUserId + 1)
//                .mapToDouble(i -> Math.min(
//                        userActionWeightsByFirstEventId.getOrDefault(i, 0.0),
//                        userActionWeightsBySecondEventId.getOrDefault(i, 0.0)
//                ))
//                .sum();
//
//        sumMinimumWeightsUserAction
//                .computeIfAbsent(firstEventId, k -> new HashMap<>())
//                .put(secondEventId, minSum);
//    }