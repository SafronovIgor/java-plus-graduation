package ru.practicum.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.practicum.model.ServiceHit;

public interface StatRepository extends JpaRepository<ServiceHit, Long>, HitListElementRepository {

}
