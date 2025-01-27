package ru.practicum.configuration;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.serialization.VoidDeserializer;
import org.springframework.boot.context.properties.ConfigurationProperties;
import ru.practicum.ewm.stats.avro.EventSimilarityAvro;

import java.util.Map;
import java.util.Properties;

import static org.apache.kafka.clients.consumer.ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG;

@Getter
@Setter
@NoArgsConstructor
@ConfigurationProperties("analyzer.kafka.consumer.events-similarity")
public class EventSimilarityConsumerConfig {
    private Map<String, String> properties;
    private Map<String, String> topics;

    public Map<String, String> getProperties() {
        return properties == null ? Map.of() : Map.copyOf(properties);
    }

    public Map<String, String> getTopics() {
        return topics == null ? Map.of() : Map.copyOf(topics);
    }

    public Consumer<Void, EventSimilarityAvro> getConsumer() {
        return new KafkaConsumer<>(getPropertiesForKafkaConsumer());
    }

    private Properties getPropertiesForKafkaConsumer() {
        Properties config = new Properties();
        for (String key : properties.keySet()) {
            config.put(key, properties.get(key));
        }
        config.put(KEY_DESERIALIZER_CLASS_CONFIG, VoidDeserializer.class.getCanonicalName());
        return config;
    }
}