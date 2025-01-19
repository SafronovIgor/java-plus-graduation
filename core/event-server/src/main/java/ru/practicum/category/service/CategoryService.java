package ru.practicum.category.service;

import ru.practicum.dto.category.CategoryDto;
import ru.practicum.dto.category.NewCategoryDto;
import ru.practicum.dto.category.UpdateCategoryDto;

import java.util.List;

public interface CategoryService {
    CategoryDto addCategory(NewCategoryDto category);

    void deleteCategory(long catId);

    CategoryDto updateCategory(long catId, UpdateCategoryDto category);

    List<CategoryDto> getAllCategories(int from, int size);

    CategoryDto getCategory(long catId);
}