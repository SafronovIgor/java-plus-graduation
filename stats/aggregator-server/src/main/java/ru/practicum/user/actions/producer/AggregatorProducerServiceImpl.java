package ru.practicum.user.actions.producer;

import lombok.RequiredArgsConstructor;
import org.apache.avro.specific.SpecificRecordBase;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.springframework.stereotype.Component;
import ru.practicum.configuration.ProducerConfig;
import ru.practicum.ewm.stats.avro.EventSimilarityAvro;

@Component
@RequiredArgsConstructor
public class AggregatorProducerServiceImpl implements AggregatorProducerService {
    private final ProducerConfig producerConfig;

    @Override
    public void aggregateUserActions(EventSimilarityAvro message) {
        Producer<String, SpecificRecordBase> producer = producerConfig.getKafkaProducer();
        ProducerRecord<String, SpecificRecordBase> record = new ProducerRecord<>(
                producerConfig.getTopics().get("events.similarity"),
                message
        );
        producer.send(record);
    }
}