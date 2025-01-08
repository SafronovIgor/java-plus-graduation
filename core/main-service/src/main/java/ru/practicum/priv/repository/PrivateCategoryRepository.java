package ru.practicum.priv.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.practicum.model.Category;

public interface PrivateCategoryRepository extends JpaRepository<Category, Long> {

}