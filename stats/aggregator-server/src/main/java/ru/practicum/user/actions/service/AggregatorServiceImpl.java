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