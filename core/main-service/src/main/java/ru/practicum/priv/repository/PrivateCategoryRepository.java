package ru.practicum.priv.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.practicum.model.Category;

@Repository
public interface PrivateCategoryRepository extends JpaRepository<Category, Long> {
}
