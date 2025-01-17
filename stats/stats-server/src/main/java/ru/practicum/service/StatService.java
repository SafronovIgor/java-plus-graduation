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
import java.util.List;

@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
@Slf4j
public class StatService {
    private final StatRepository statRepository;
    private final ServiceHitMapper serviceHitMapper;

    public StatRequestDto registerHit(StatRequestDto statRequestDto) {
        ServiceHit entity = serviceHitMapper.toEntity(statRequestDto);
        log.info("StatService converted entity: {}", entity);
        ServiceHit saved = statRepository.save(entity);
        log.info("StatService saved entity: {}", saved);
        StatRequestDto dto = serviceHitMapper.toDto(saved);
        log.info("StatService converted dto: {}", dto);
        return dto;
    }

    public List<StatResponseDto> getHits(LocalDateTime start, LocalDateTime end, String[] uris, Boolean unique) {
        List<StatResponseDto> statResponseDtos;
        statResponseDtos = statRepository.getHitListElementDtos(start, end, uris, unique);
        return statResponseDtos;
    }
}
