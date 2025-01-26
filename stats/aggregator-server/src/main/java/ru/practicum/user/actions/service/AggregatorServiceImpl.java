package ru.practicum.user.actions.service;

import lombok.RequiredArgsConstructor;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.stereotype.Service;
import ru.practicum.ewm.stats.avro.ActionTypeAvro;
import ru.practicum.ewm.stats.avro.EventSimilarityAvro;
import ru.practicum.ewm.stats.avro.UserActionAvro;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static ru.practicum.WeightCoefficients.*;

@Service
@RequiredArgsConstructor
public class AggregatorServiceImpl implements AggregatorService {
    private final Map<Long, Double> sumWeightsEvents = new HashMap<>();
    private final Map<Long, Map<Long, Double>> matrixWeightsUserAction = new HashMap<>();

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
                return calculateEventSimilarity(userActionAvro);
            }
        }

        addNewUserActionWeight(eventId, userId, true, actionType);
        return calculateEventSimilarity(userActionAvro);
    }

    private void addNewUserActionWeight(long eventId, long userId, boolean calculateSimilarity, ActionTypeAvro type) {
        var actionWeight = calculateSimilarity ? ABSENCE_OF_ACTIONS_RATIO : getWeight(type);

        matrixWeightsUserAction.computeIfAbsent(eventId, k -> new HashMap<>()).put(userId, actionWeight);
        sumWeightsEvents.put(eventId, actionWeight);
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

    private List<EventSimilarityAvro> calculateEventSimilarity(UserActionAvro userActionAvro) {
        //todo: тут прям харда
        return List.of();
    }
}