package ru.practicum.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.constants.DataTransferConvention;
import ru.practicum.dto.StatRequestDto;
import ru.practicum.dto.StatResponseDto;
import ru.practicum.service.StatService;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@Validated
@RequiredArgsConstructor
@Slf4j
public class StatController {
    private final StatService statService;

    @PostMapping("/hit")
    public ResponseEntity<StatRequestDto> registerHit(@RequestBody @Valid StatRequestDto statRequestDto) {
        log.info("Registering hit: {}", statRequestDto);
        return new ResponseEntity<>(statService.registerHit(statRequestDto), HttpStatus.CREATED);
    }

    @GetMapping("/stats")
    public ResponseEntity<List<StatResponseDto>> getStats(
            @RequestParam("start") @DateTimeFormat(pattern = DataTransferConvention.DATE_TIME_PATTERN) LocalDateTime start,
            @RequestParam("end") @DateTimeFormat(pattern = DataTransferConvention.DATE_TIME_PATTERN) LocalDateTime end,
            @RequestParam(value = "uris", required = false) String[] uris,
            @RequestParam(value = "unique", defaultValue = "false")
            Boolean unique) {
        if (start.isAfter(end)) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
        return new ResponseEntity<>(statService.getHits(start, end, uris, unique), HttpStatus.OK);
    }
}
