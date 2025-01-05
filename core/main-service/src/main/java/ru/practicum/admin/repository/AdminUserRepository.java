package ru.practicum.admin.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import ru.practicum.dto.user.UserDto;
import ru.practicum.model.User;

import java.util.List;

@Repository
public interface AdminUserRepository extends JpaRepository<User, Long> {
    @Query("select new ru.practicum.dto.user.UserDto(u.id, u.name, u.email) from User u where u.id in (:ids)")
    Page<UserDto> findUserDtosByIds(List<Long> ids, Pageable pageable);

    Boolean existsByEmail(String email);
}
