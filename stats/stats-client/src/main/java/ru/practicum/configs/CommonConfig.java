package ru.practicum.configs;

import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;
import ru.practicum.HttpStatsClient;
import ru.practicum.HttpStatsClientImpl;
import ru.practicum.HttpStatsClientLoggingDecorator;
import ru.practicum.feign.StatsServerHttpClient;

@Configuration
@EnableFeignClients(basePackages = "ru.practicum.feign")
public class CommonConfig {

    @Bean
    public RestTemplate createRestTemplate() {
        return new RestTemplate();
    }

    @Bean
    public HttpStatsClient createHttpStatsServer(RestTemplate restTemplate,
                                                 StatsServerHttpClient statsServerHttpClient) {
        var httpStatsServer = new HttpStatsClientImpl(restTemplate, statsServerHttpClient);
        return new HttpStatsClientLoggingDecorator(httpStatsServer);
    }
}