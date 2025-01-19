package ru.practicum.category.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.category.mapper.CategoryMapper;
import ru.practicum.category.model.Category;
import ru.practicum.category.repository.CategoryRepository;
import ru.practicum.dto.category.CategoryDto;
import ru.practicum.dto.category.NewCategoryDto;
import ru.practicum.dto.category.UpdateCategoryDto;
import ru.practicum.event.repository.EventRepository;
import ru.practicum.exception.IntegrityViolationException;
import ru.practicum.exception.NotFoundException;

import java.util.Collections;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class CategoryServiceImpl implements CategoryService {
    private final CategoryRepository categoryRepository;
    private final EventRepository eventRepository;
    private final CategoryMapper categoryMapper;

    @Override
    @Transactional
    public CategoryDto addCategory(NewCategoryDto newCategoryDto) {
        log.info("Adding new category with name: {}", newCategoryDto.getName());
        Category category = categoryMapper.newCategoryDtoToCategory(newCategoryDto);
        verifyCategoryNameNotExists(category.getName());
        Category createdCategory = categoryRepository.save(category);
        CategoryDto result = categoryMapper.categoryToCategoryDto(createdCategory);
        log.info("Successfully added category: {}", result);
        return result;
    }

    @Override
    @Transactional
    public void deleteCategory(long catId) {
        log.info("Deleting category with ID: {}", catId);
        getCategoryById(catId);
        verifyCategoryNotUsedInEvents(catId);
        categoryRepository.deleteById(catId);
        log.info("Successfully deleted category with ID: {}", catId);
    }

    @Override
    @Transactional
    public CategoryDto updateCategory(long catId, UpdateCategoryDto newCategory) {
        log.info("Updating category with ID: {}", catId);
        Category existingCategory = getCategoryById(catId);
        verifyCategoryNameNotExistsForUpdate(newCategory.getName(), catId);
        existingCategory.setName(newCategory.getName());
        CategoryDto updatedCategoryDto = categoryMapper.categoryToCategoryDto(existingCategory);
        log.info("Successfully updated category: {}", updatedCategoryDto);
        return updatedCategoryDto;
    }

    @Override
    @Transactional(readOnly = true)
    public List<CategoryDto> getAllCategories(int from, int size) {
        log.info("Fetching all categories with pagination: from = {}, size = {}", from, size);
        PageRequest pageRequest = PageRequest.of(from, size);
        Page<Category> pageCategories = categoryRepository.findAll(pageRequest);
        List<Category> categories = pageCategories.hasContent() ? pageCategories.getContent() : Collections.emptyList();
        List<CategoryDto> categoryDtos = categoryMapper.listCategoryToListCategoryDto(categories);
        log.info("Successfully fetched {} categories", categoryDtos.size());
        return categoryDtos;
    }

    @Override
    @Transactional(readOnly = true)
    public CategoryDto getCategory(long catId) {
        log.info("Fetching category with ID: {}", catId);
        Category category = getCategoryById(catId);
        CategoryDto result = categoryMapper.categoryToCategoryDto(category);
        log.info("Successfully fetched category: {}", result);
        return result;
    }

    private Category getCategoryById(long catId) {
        log.debug("Fetching category from repository by ID: {}", catId);
        return categoryRepository.findById(catId)
                .orElseThrow(() -> {
                    log.error("Category with ID: {} not found", catId);
                    return new NotFoundException("Category with id = " + catId + " does not exist");
                });
    }

    private void verifyCategoryNameNotExists(String name) {
        log.debug("Verifying if category name '{}' already exists", name);
        categoryRepository.findCategoriesByNameContainingIgnoreCase(name.toLowerCase()).ifPresent(c -> {
            log.error("Category with name '{}' already exists", name);
            throw new IntegrityViolationException("Category name " + name + " already exists");
        });
        log.debug("Category name '{}' does not exist", name);
    }

    private void verifyCategoryNameNotExistsForUpdate(String name, long currentCategoryId) {
        log.debug("Verifying if category name '{}' is already taken for a different category", name);
        categoryRepository.findCategoriesByNameContainingIgnoreCase(name.toLowerCase()).ifPresent(c -> {
            if (c.getId() != currentCategoryId) {
                log.error("Category name '{}' is already used by category with ID: {}", name, c.getId());
                throw new IntegrityViolationException("Category name " + name + " already exists");
            }
        });
        log.debug("Category name '{}' is available for update", name);
    }

    private void verifyCategoryNotUsedInEvents(long catId) {
        log.debug("Verifying if category with ID: {} is not used in any events", catId);
        if (!eventRepository.findAllByCategoryId(catId).isEmpty()) {
            log.error("Category with ID: {} is used in existing events", catId);
            throw new IntegrityViolationException("Category with id = " + catId
                    + " is used in events and cannot be deleted");
        }
        log.debug("Category with ID: {} is not used in any events", catId);
    }
}