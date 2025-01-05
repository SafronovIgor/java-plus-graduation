package ru.practicum.pub.repository;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import ru.practicum.dto.category.CategoryDto;
import ru.practicum.model.Category;

import java.util.List;

@Repository
public interface PublicCategoryRepository extends JpaRepository<Category, Long> {
    @Query("select new ru.practicum.dto.category.CategoryDto(c.id, c.name) from Category c")
    List<CategoryDto> getCategoryDtos(Pageable pageable);
}
