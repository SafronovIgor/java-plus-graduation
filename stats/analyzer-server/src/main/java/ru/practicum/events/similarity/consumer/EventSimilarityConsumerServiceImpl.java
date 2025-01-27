package ru.practicum.events.similarity.consumer;

import lombok.RequiredArgsConstructor;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.common.errors.WakeupException;
import org.springframework.stereotype.Component;
import ru.practicum.configuration.EventSimilarityConsumerConfig;
import ru.practicum.events.similarity.service.EventSimilarityService;
import ru.practicum.ewm.stats.avro.EventSimilarityAvro;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

@Component
@RequiredArgsConstructor
public class EventSimilarityConsumerServiceImpl implements EventSimilarityConsumerService {
    private static final int MILLIS = 300;
    private final EventSimilarityConsumerConfig consumerConfig;
    private final EventSimilarityService eventSimilarityService;

    @Override
    public void consumeEventsSimilarity() {
        try (Consumer<Void, EventSimilarityAvro> consumer = consumerConfig.getConsumer();) {
            Runtime.getRuntime().addShutdownHook(new Thread(consumer::wakeup));
            List<EventSimilarityAvro> eventsSimilarityAvro = new ArrayList<>();

            consumer.subscribe(List.of(consumerConfig.getTopics().get("events.similarity")));

            while (true) {
                var records = consumer.poll(Duration.ofMillis(MILLIS));

                for (ConsumerRecord<Void, EventSimilarityAvro> record : records) {
                    eventsSimilarityAvro.add(record.value());
                }

                eventSimilarityService.saveEventsSimilarity(eventsSimilarityAvro);
                consumer.commitSync();
            }
        } catch (WakeupException ignored) {

        }
    }
}
