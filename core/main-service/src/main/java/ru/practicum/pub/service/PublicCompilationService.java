package ru.practicum.pub.service;

import ru.practicum.dto.compilation.CompilationDto;

import java.util.List;

public interface PublicCompilationService {
    List<CompilationDto> getCompilations(Boolean pinned, Integer from, Integer size);

    CompilationDto getCompilation(Long compId);
}