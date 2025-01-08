package ru.practicum.admin.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.practicum.model.Locus;

public interface AdminLocusRepository extends JpaRepository<Locus, Long> {

}