package ru.practicum.user.actions.consumer;

import lombok.RequiredArgsConstructor;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.common.errors.WakeupException;
import org.springframework.stereotype.Component;
import ru.practicum.configuration.ConsumerConfig;
import ru.practicum.ewm.stats.avro.EventSimilarityAvro;
import ru.practicum.ewm.stats.avro.UserActionAvro;
import ru.practicum.user.actions.producer.AggregatorProducerService;
import ru.practicum.user.actions.service.AggregatorService;

import java.time.Duration;
import java.util.List;

@Component
@RequiredArgsConstructor
public class AggregatorConsumerServiceImpl implements AggregatorConsumerService {
    public static final int MILLIS = 5_000;
    private final ConsumerConfig consumerConfig;
    private final AggregatorService aggregatorService;
    private final AggregatorProducerService aggregatorProducerService;

    @Override
    public void consumeUserActions() {
        Consumer<Void, UserActionAvro> consumer = consumerConfig.getKafkaConsumer();

        try (consumer) {
            Runtime.getRuntime().addShutdownHook(new Thread(consumer::wakeup));
            consumer.subscribe(List.of(consumerConfig.getTopics().get("user.actions")));

            while (true) {
                ConsumerRecords<Void, UserActionAvro> records = consumer.poll(Duration.ofMillis(MILLIS));

                for (ConsumerRecord<Void, UserActionAvro> record : records) {
                    List<EventSimilarityAvro> eventsSimilarityAvro = aggregatorService.handle(record);

                    if (!eventsSimilarityAvro.isEmpty()) {
                        for (EventSimilarityAvro eventSimilarityAvro : eventsSimilarityAvro) {
                            aggregatorProducerService.aggregateUserActions(eventSimilarityAvro);
                        }
                    }
                }

                consumer.commitAsync();
            }
        } catch (WakeupException ignored) {
        }
    }
}