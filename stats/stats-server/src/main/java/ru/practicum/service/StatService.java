package ru.practicum.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.practicum.dto.StatRequestDto;
import ru.practicum.dto.StatResponseDto;
import ru.practicum.mapper.ServiceHitMapper;
import ru.practicum.model.ServiceHit;
import ru.practicum.repository.StatRepository;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
@Slf4j
public class StatService {
    private final StatRepository statRepository;
    private final ServiceHitMapper serviceHitMapper;

    public StatRequestDto registerHit(StatRequestDto statRequestDto) {
        log.info("Registering hit: {}", statRequestDto);

        ServiceHit entity = serviceHitMapper.toEntity(statRequestDto);
        log.debug("Mapped StatRequestDto to ServiceHit entity: {}", entity);

        ServiceHit saved = statRepository.save(entity);
        log.debug("Saved ServiceHit entity: {}", saved);

        StatRequestDto dto = serviceHitMapper.toDto(saved);
        log.debug("Mapped ServiceHit entity back to StatRequestDto: {}", dto);

        log.info("Hit registration completed successfully. Returning DTO: {}", dto);
        return dto;
    }

    public List<StatResponseDto> getHits(LocalDateTime start, LocalDateTime end, String[] uris, Boolean unique) {
        log.info("Retrieving hits with the following parameters: start={}, end={}, uris={}, unique={}",
                start, end, Arrays.toString(uris), unique);

        log.debug("Starting query in repository with parameters: start={}, end={}, uris={}, unique={}",
                start, end, Arrays.toString(uris), unique);

        List<StatResponseDto> statResponseDtos = statRepository.getHitListElementDtos(start, end, uris, unique);

        log.debug("Retrieved {} records from repository: {}", statResponseDtos.size(), statResponseDtos);

        log.info("Completed retrieving hits. Total records found: {}", statResponseDtos.size());
        return statResponseDtos;
    }
}