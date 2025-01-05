package ru.practicum.pub.controller;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.practicum.DataTransferConvention;
import ru.practicum.dto.category.CategoryDto;
import ru.practicum.pub.service.PublicCategoryService;

import java.util.List;

@RestController
@RequestMapping("/categories")
@RequiredArgsConstructor
public class PublicCategoryController {
    private final PublicCategoryService publicCategoryService;

    @GetMapping
    public ResponseEntity<List<CategoryDto>> getCategories(
            @RequestParam(required = false, defaultValue = DataTransferConvention.FROM) Integer from,
            @RequestParam(required = false, defaultValue = DataTransferConvention.SIZE) Integer size) {
        return new ResponseEntity<>(publicCategoryService.getCategories(from, size), HttpStatus.OK);
    }

    @GetMapping("/{catId}")
    public ResponseEntity<CategoryDto> getCategory(@PathVariable @Min(1) @NotNull Long catId) {
        return new ResponseEntity<>(publicCategoryService.getCategory(catId), HttpStatus.OK);
    }
}
