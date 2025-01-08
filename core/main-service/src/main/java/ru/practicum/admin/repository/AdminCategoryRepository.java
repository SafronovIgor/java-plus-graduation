package ru.practicum.admin.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.practicum.model.Category;

public interface AdminCategoryRepository extends JpaRepository<Category, Long> {
    boolean existsByName(String name);
}