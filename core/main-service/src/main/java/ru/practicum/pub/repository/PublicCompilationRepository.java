package ru.practicum.pub.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.practicum.model.Compilation;

import java.util.Optional;

public interface PublicCompilationRepository extends JpaRepository<Compilation, Integer>, CompilationDtoRepository {
    Optional<Compilation> findById(Long id);
}
