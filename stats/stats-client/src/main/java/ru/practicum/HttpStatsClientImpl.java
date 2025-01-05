package ru.practicum;

import lombok.RequiredArgsConstructor;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;
import ru.practicum.dto.StatRequestDto;
import ru.practicum.dto.StatResponseDto;
import ru.practicum.feign.StatsServerHttpClient;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static ru.practicum.constants.DataTransferConvention.*;

@RequiredArgsConstructor
public class HttpStatsClientImpl implements HttpStatsClient {
    private final RestTemplate restTemplate;
    private final StatsServerHttpClient statsServerHttpClient;

    public List<StatResponseDto> getStats(String start, String end, List<String> uris, Boolean unique) {
        return statsServerHttpClient.getStats(LocalDateTime.parse(start, DATE_TIME_FORMATTER),
                LocalDateTime.parse(end, DATE_TIME_FORMATTER),
                uris.toArray(new String[0]),
                unique
        );
    }


    @Override
    public <R> Optional<R> getStats(StatsParameters<R> param) {
        return Optional.ofNullable(restTemplate.getForObject(UriComponentsBuilder.fromHttpUrl(STAT_SERVICE_URL)
                .path(STATS_PATH)
                .queryParam("start", param.getStart())
                .queryParam("end", param.getEnd())
                .queryParam("uris", param.getUris())
                .queryParam("unique", param.isUnique())
                .build().toUri(), param.getResponseType()));
    }

    @Override
    public <T, R> Optional<R> sendHit(T hit, Class<R> responseType) {
        return Optional.ofNullable(responseType.cast(statsServerHttpClient.registerHit((StatRequestDto) hit)));
    }
}