package ru.practicum.admin.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.practicum.model.Compilation;

public interface AdminCompilationRepository extends JpaRepository<Compilation, Long> {

}