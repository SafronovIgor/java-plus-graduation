package ru.practicum.feign;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import ru.practicum.constants.DataTransferConvention;
import ru.practicum.dto.StatRequestDto;
import ru.practicum.dto.StatResponseDto;

import java.time.LocalDateTime;
import java.util.List;

@FeignClient(name = "stats-server")
public interface StatsServerHttpClient {

    @PostMapping("/hit")
    StatRequestDto registerHit(@RequestBody StatRequestDto statRequestDto);

    @GetMapping("/stats")
    List<StatResponseDto> getStats(@RequestParam("start") @DateTimeFormat(pattern = DataTransferConvention.DATE_TIME_PATTERN) LocalDateTime start,
                                   @RequestParam("end") @DateTimeFormat(pattern = DataTransferConvention.DATE_TIME_PATTERN) LocalDateTime end,
                                   @RequestParam(value = "uris", required = false) String[] uris,
                                   @RequestParam(value = "unique", defaultValue = "false") Boolean unique);
}
