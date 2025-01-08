package ru.practicum.admin.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.admin.repository.AdminCategoryRepository;
import ru.practicum.dto.category.CategoryDto;
import ru.practicum.dto.category.NewCategoryDto;
import ru.practicum.exception.ConflictException;
import ru.practicum.exception.NotFoundException;
import ru.practicum.model.Category;


@Service
@RequiredArgsConstructor
@Transactional
public class AdminCategoryServiceImpl implements AdminCategoryService {
    private final AdminCategoryRepository adminCategoryRepository;

    @Override
    public CategoryDto addCategory(NewCategoryDto newCategoryDto) {
        String name = newCategoryDto.getName();
        if (adminCategoryRepository.existsByName(name)) {
            throw new ConflictException("Category with name " + name + " already exists");
        }
        Category category = new Category();
        category.setName(name);
        category = adminCategoryRepository.save(category);
        return new CategoryDto(category.getId(), category.getName());
    }

    @Override
    public void deleteCategory(Long catId) {
        if (!adminCategoryRepository.existsById(catId)) {
            throw new NotFoundException("Category with id " + catId + " does not exist");
        }
        adminCategoryRepository.deleteById(catId);
    }

    @Override
    public CategoryDto updateCategory(Long catId, NewCategoryDto newCategoryDto) {
        Category found = adminCategoryRepository.findById(catId)
                .orElseThrow(() -> new NotFoundException("Category with id " + catId + " does not exist"));
        String name = newCategoryDto.getName();
        if (!found.getName().equals(name) && adminCategoryRepository.existsByName(name)) {
            throw new ConflictException("Category with name " + name + " already exists");
        }
        found.setName(name);
        return new CategoryDto(found.getId(), found.getName());
    }

}
