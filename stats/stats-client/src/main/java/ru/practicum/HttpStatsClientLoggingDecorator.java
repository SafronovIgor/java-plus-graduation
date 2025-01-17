package ru.practicum;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.client.RestClientException;
import ru.practicum.dto.StatResponseDto;

import java.util.List;
import java.util.Optional;

@Slf4j
public class HttpStatsClientLoggingDecorator implements HttpStatsClient {

    private final HttpStatsClient delegate;

    public HttpStatsClientLoggingDecorator(HttpStatsClient delegate) {
        this.delegate = delegate;
    }

    @Override
    public List<StatResponseDto> getStats(String start, String end, List<String> uris, Boolean unique) {
        return delegate.getStats(start, end, uris, unique);
    }

    @Override
    public <R> Optional<R> getStats(StatsParameters<R> params) {
        log.info("Getting stats: {}", params.toString());
        try {
            var optResult = delegate.getStats(params);
            if (optResult.isPresent()) {
                log.info("Stats received: {}", optResult.get());
            } else {
                log.info("Stats received: no stats available");
            }
            return optResult;
        } catch (RestClientException e) {
            log.error("Error during fetching stats: {}", e.getMessage(), e);
            return Optional.empty();
        }
    }

    @Override
    public <T, R> Optional<R> sendHit(T hitDto, Class<R> responseType) {
        log.info("Sending hitDto: {}", hitDto);
        try {
            var optResult = delegate.sendHit(hitDto, responseType);
            if (optResult.isPresent()) {
                log.info("Hit response: {}", optResult.get());
            } else {
                log.info("Hit response: no response received");
            }
            return optResult;
        } catch (RestClientException e) {
            log.error("Error during fetching hitDto: {}", e.getMessage(), e);
            return Optional.empty();
        }
    }
}