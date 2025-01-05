package ru.practicum.pub.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.practicum.model.Compilation;

import java.util.Optional;

@Repository
public interface PublicCompilationRepository extends JpaRepository<Compilation, Integer>, CompilationDtoRepository {
    Optional<Compilation> findById(Long id);
}
