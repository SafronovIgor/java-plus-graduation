package ru.practicum.pub.service;

import ru.practicum.dto.category.CategoryDto;

import java.util.List;

public interface PublicCategoryService {
    List<CategoryDto> getCategories(Integer from, Integer size);

    CategoryDto getCategory(Long catId);
}