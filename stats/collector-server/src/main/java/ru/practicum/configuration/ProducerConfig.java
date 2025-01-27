package ru.practicum.configuration;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.apache.avro.specific.SpecificRecordBase;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.Map;
import java.util.Properties;

@Getter
@Setter
@NoArgsConstructor
@ConfigurationProperties("collector.kafka.producer")
public class ProducerConfig {
    private Map<String, String> properties;
    private Map<String, String> topics;

    public Map<String, String> getProperties() {
        return properties == null ? Map.of() : Map.copyOf(properties);
    }

    public Map<String, String> getTopics() {
        return topics == null ? Map.of() : Map.copyOf(topics);
    }

    public Producer<String, SpecificRecordBase> getKafkaProducer() {
        return new KafkaProducer<>(getPropertiesForKafkaProducer());
    }

    private Properties getPropertiesForKafkaProducer() {
        Properties cfg = new Properties();
        for (String k : properties.keySet()) {
            cfg.put(k, properties.get(k));
        }
        return cfg;
    }
}