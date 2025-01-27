package ru.practicum;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.context.ConfigurableApplicationContext;
import ru.practicum.user.actions.consumer.AggregatorConsumerService;

@SpringBootApplication
@ConfigurationPropertiesScan("ru.practicum.configuration")
public class AggregatorService {
    public static void main(String[] args) {
        ConfigurableApplicationContext context = SpringApplication.run(AggregatorService.class, args);
        AggregatorConsumerService actionConsumer = context.getBean(AggregatorConsumerService.class);
        actionConsumer.consumeUserActions();
    }
}