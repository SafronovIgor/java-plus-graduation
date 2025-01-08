package ru.practicum.pub.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.practicum.DataTransferConvention;
import ru.practicum.dto.compilation.CompilationDto;
import ru.practicum.pub.service.PublicCompilationService;

import java.util.List;

@RestController
@RequestMapping("/compilations")
@RequiredArgsConstructor
public class PublicCompilationController {
    private final PublicCompilationService publicCompilationService;

    /**
     * В случае, если по заданным фильтрам не найдено ни одной подборки, возвращает пустой список
     */
    @GetMapping
    public ResponseEntity<List<CompilationDto>> getCompilations(
            @RequestParam(defaultValue = "false") Boolean pinned,
            @RequestParam(defaultValue = DataTransferConvention.FROM) Integer from,
            @RequestParam(defaultValue = DataTransferConvention.SIZE) Integer size) {

        return new ResponseEntity<>(publicCompilationService.getCompilations(pinned, from, size), HttpStatus.OK);
    }

    @GetMapping("/{compId}")
    public ResponseEntity<CompilationDto> getCompilation(@PathVariable Long compId) {
        return new ResponseEntity<>(publicCompilationService.getCompilation(compId), HttpStatus.OK);
    }
}
