package ru.practicum.user.actions.producer;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.avro.specific.SpecificRecordBase;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.springframework.stereotype.Component;
import ru.practicum.configuration.ProducerConfig;
import ru.practicum.ewm.stats.avro.UserActionAvro;
import ru.practicum.ewm.stats.proto.UserActionProto;
import ru.practicum.user.actions.mapper.AvroMapper;

@Slf4j
@Component
@RequiredArgsConstructor
public class UserActionProducerImpl implements UserActionProducer {
    private final ProducerConfig producerConfig;

    @Override
    public void collectUserAction(UserActionProto userActionProto) {
        Producer<String, SpecificRecordBase> producer = producerConfig.getKafkaProducer();
        UserActionAvro message = AvroMapper.convertToAvro(userActionProto);
        ProducerRecord<String, SpecificRecordBase> record = new ProducerRecord<>(
                producerConfig.getTopics().get("user.actions"),
                message
        );
        producer.send(record);
    }
}