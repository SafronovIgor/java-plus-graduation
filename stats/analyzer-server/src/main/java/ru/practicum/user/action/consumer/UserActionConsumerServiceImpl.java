package ru.practicum.user.action.consumer;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.common.errors.WakeupException;
import org.springframework.stereotype.Component;
import ru.practicum.configuration.UserActionConsumerConfig;
import ru.practicum.ewm.stats.avro.UserActionAvro;
import ru.practicum.user.action.service.UserActionService;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class UserActionConsumerServiceImpl implements UserActionConsumerService {
    private static final int MILLIS = 300;
    private final UserActionService userActionsService;
    private final UserActionConsumerConfig consumerConfig;

    @Override
    public void consumeUserActions() {

        try (Consumer<Void, UserActionAvro> consumer = consumerConfig.getConsumer()) {
            Runtime.getRuntime().addShutdownHook(new Thread(consumer::wakeup));

            consumer.subscribe(List.of(consumerConfig.getTopics().get("user.actions")));
            log.info("Subscribed to topic: {}", consumerConfig.getTopics().get("user_actions"));

            while (true) {
                var records = consumer.poll(Duration.ofMillis(MILLIS));
                if (records.isEmpty()) {
                    log.debug("No records found during polling.");
                    continue;
                }

                log.info("Fetched {} records from Kafka.", records.count());

                List<UserActionAvro> userActions = new ArrayList<>();
                for (ConsumerRecord<Void, UserActionAvro> record : records) {
                    userActions.add(record.value());
                }

                try {
                    userActionsService.saveUserActions(userActions);
                    log.info("Successfully processed {} records.", userActions.size());
                } catch (Exception e) {
                    log.error("Error saving user actions: {}", e.getMessage(), e);
                }

                try {
                    consumer.commitSync();
                    log.info("Successfully committed {} records.", records.count());
                } catch (Exception e) {
                    log.error("Error during commit: {}", e.getMessage(), e);
                }
            }
        } catch (WakeupException e) {
            log.info("Consumer shutdown initiated.");
        } catch (Exception e) {
            log.error("Unexpected error in consumer: {}", e.getMessage(), e);
        } finally {
            log.info("Consumer closed.");
        }
    }
}