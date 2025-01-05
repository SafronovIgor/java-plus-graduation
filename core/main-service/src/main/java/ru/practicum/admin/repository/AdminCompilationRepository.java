package ru.practicum.admin.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.practicum.model.Compilation;


@Repository
public interface AdminCompilationRepository extends JpaRepository<Compilation, Long> {

}
