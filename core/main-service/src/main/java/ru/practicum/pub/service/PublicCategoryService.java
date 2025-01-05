package ru.practicum.pub.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import ru.practicum.dto.category.CategoryDto;
import ru.practicum.exception.NotFoundException;
import ru.practicum.model.Category;
import ru.practicum.pub.repository.PublicCategoryRepository;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PublicCategoryService {
    private final PublicCategoryRepository publicCategoryRepository;

    public List<CategoryDto> getCategories(Integer from, Integer size) {
        Pageable pageable = PageRequest.of(from, size);
        return publicCategoryRepository.getCategoryDtos(pageable);
    }

    public CategoryDto getCategory(Long catId) {
        Category found =
                publicCategoryRepository.findById(catId).orElseThrow(() -> new NotFoundException("Category not found"));
        return new CategoryDto(found.getId(), found.getName());
    }
}
