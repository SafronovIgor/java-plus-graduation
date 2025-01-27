package ru.practicum.configuration;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.serialization.VoidDeserializer;
import org.springframework.boot.context.properties.ConfigurationProperties;
import ru.practicum.ewm.stats.avro.UserActionAvro;

import java.util.Map;
import java.util.Properties;

import static org.apache.kafka.clients.consumer.ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG;

@Getter
@Setter
@NoArgsConstructor
@ConfigurationProperties("aggregator.kafka.consumer")
public class ConsumerConfig {
    private Map<String, String> properties;
    private Map<String, String> topics;

    public Map<String, String> getProperties() {
        return properties == null ? Map.of() : Map.copyOf(properties);
    }

    public Map<String, String> getTopics() {
        return topics == null ? Map.of() : Map.copyOf(topics);
    }

    public Consumer<Void, UserActionAvro> getKafkaConsumer() {
        return new KafkaConsumer<>(getPropertiesForKafkaConsumer());
    }

    private Properties getPropertiesForKafkaConsumer() {
        Properties props = new Properties();
        for (String k : properties.keySet()) {
            props.put(k, properties.get(k));
        }
        props.put(KEY_DESERIALIZER_CLASS_CONFIG, VoidDeserializer.class.getCanonicalName());
        return props;
    }
}