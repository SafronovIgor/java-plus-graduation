package ru.practicum.admin.controller;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.admin.service.AdminCategoryService;
import ru.practicum.dto.category.CategoryDto;
import ru.practicum.dto.category.NewCategoryDto;

@RestController
@RequestMapping("/admin/categories")
@Validated
@RequiredArgsConstructor
public class AdminCategoryController {
    private final AdminCategoryService adminCategoryService;

    @PostMapping
    public ResponseEntity<CategoryDto> addCategory(@RequestBody @Valid NewCategoryDto newCategoryDto) {
        return new ResponseEntity<>(adminCategoryService.addCategory(newCategoryDto), HttpStatus.CREATED);
    }

    @DeleteMapping("/{catId}")
    public ResponseEntity<CategoryDto> deleteCategory(@PathVariable @Min(1) Long catId) {
        adminCategoryService.deleteCategory(catId);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{catId}")
    public ResponseEntity<CategoryDto> updateCategory(@PathVariable @Min(1) Long catId,
                                                      @RequestBody @Valid NewCategoryDto newCategoryDto) {
        return new ResponseEntity<>(adminCategoryService.updateCategory(catId, newCategoryDto), HttpStatus.OK);
    }
}
