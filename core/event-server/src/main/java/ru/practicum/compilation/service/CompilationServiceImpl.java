package ru.practicum.compilation.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import ru.practicum.compilation.mapper.CompilationMapper;
import ru.practicum.compilation.model.Compilation;
import ru.practicum.compilation.repository.CompilationRepository;
import ru.practicum.dto.compilation.CompilationDto;
import ru.practicum.dto.compilation.NewCompilationDto;
import ru.practicum.dto.compilation.UpdateCompilationRequest;
import ru.practicum.event.model.Event;
import ru.practicum.event.repository.EventRepository;
import ru.practicum.exception.NotFoundException;

import java.util.Collections;
import java.util.List;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class CompilationServiceImpl implements CompilationService {
    private final CompilationRepository compilationRepository;
    private final CompilationMapper compilationMapper;
    private final EventRepository eventRepository;

    @Override
    public CompilationDto addCompilation(NewCompilationDto compilationDto) {
        log.info("Adding new compilation: {}", compilationDto);
        Compilation compilation = compilationMapper.newCompilationDtoToCompilation(compilationDto);
        List<Long> eventIds = compilationDto.getEvents();
        var events = fetchEventsByIds(eventIds);
        log.debug("Fetched {} events for compilation", events.size());
        compilation.setEvents(events);
        Compilation createdCompilation = compilationRepository.save(compilation);
        CompilationDto result = compilationMapper.compilationToCompilationDto(createdCompilation);
        log.info("Successfully added compilation: {}", result);
        return result;
    }

    @Override
    public CompilationDto updateCompilation(long compId, UpdateCompilationRequest request) {
        log.info("Updating compilation with ID: {}", compId);
        Compilation compilation = getCompilationByIdFromRepository(compId);
        updateEventsIfPresent(compilation, request.getEvents());
        updatePinnedIfPresent(compilation, request.getPinned());
        updateTitleIfPresent(compilation, request.getTitle());
        CompilationDto result = compilationMapper.compilationToCompilationDto(compilation);
        log.info("Successfully updated compilation with ID: {}. Result: {}", compId, result);
        return result;
    }

    @Override
    public void deleteCompilation(long compId) {
        log.info("Deleting compilation with ID: {}", compId);
        getCompilationByIdFromRepository(compId);
        compilationRepository.deleteById(compId);
        log.info("Successfully deleted compilation with ID: {}", compId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CompilationDto> getAllCompilations(Boolean pinned, int from, int size) {
        log.info("Fetching compilations with pinned: {}, from: {}, size: {}", pinned, from, size);

        PageRequest pageRequest = PageRequest.of(from, size);
        List<Compilation> compilations = fetchCompilationsByPinned(pinned, pageRequest);
        List<CompilationDto> result = compilationMapper.listCompilationToListCompilationDto(compilations);

        log.info("Successfully fetched {} compilations", result.size());
        return result;
    }

    @Override
    @Transactional(readOnly = true)
    public CompilationDto getCompilationById(long compId) {
        log.info("Fetching compilation with ID: {}", compId);
        Compilation compilation = getCompilationByIdFromRepository(compId);
        CompilationDto result = compilationMapper.compilationToCompilationDto(compilation);
        log.info("Successfully fetched compilation: {}", result);
        return result;
    }

    private Compilation getCompilationByIdFromRepository(long compId) {
        log.debug("Fetching compilation from repository by ID: {}", compId);
        return compilationRepository.findById(compId)
                .orElseThrow(() -> {
                    log.error("Compilation with ID: {} not found", compId);
                    return new NotFoundException("Compilation with id " + compId + " not found");
                });
    }

    private List<Event> fetchEventsByIds(List<Long> eventIds) {
        log.debug("Fetching events by IDs: {}", eventIds);
        if (CollectionUtils.isEmpty(eventIds)) {
            log.debug("No event IDs provided, returning empty list");
            return Collections.emptyList();
        }
        var events = eventRepository.findAllByIdIn(eventIds);
        log.debug("Found {} events for provided IDs", events.size());
        return events;
    }

    private void updateEventsIfPresent(Compilation compilation, List<Long> eventIds) {
        if (!CollectionUtils.isEmpty(eventIds)) {
            log.debug("Updating events for compilation with ID: {}", compilation.getId());
            compilation.setEvents(fetchEventsByIds(eventIds));
        }
    }

    private void updatePinnedIfPresent(Compilation compilation, Boolean pinned) {
        if (pinned != null) {
            log.debug("Updating pinned status for compilation with ID: {} to {}", compilation.getId(), pinned);
            compilation.setPinned(pinned);
        }
    }

    private void updateTitleIfPresent(Compilation compilation, String title) {
        if (title != null) {
            log.debug("Updating title for compilation with ID: {} to {}", compilation.getId(), title);
            compilation.setTitle(title);
        }
    }

    private List<Compilation> fetchCompilationsByPinned(Boolean pinned, PageRequest pageRequest) {
        if (pinned == null) {
            log.debug("Fetching all compilations with no pinning filter");
            return compilationRepository.findAll(pageRequest).getContent();
        }
        log.debug("Fetching compilations with pinned = {}", pinned);
        return pinned
                ? compilationRepository.findAllByPinnedTrue(pageRequest).getContent()
                : compilationRepository.findAllByPinnedFalse(pageRequest).getContent();
    }
}