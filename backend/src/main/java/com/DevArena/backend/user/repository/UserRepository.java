package com.DevArena.backend.user.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.DevArena.backend.user.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.util.Optional;
import java.util.List;

public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByEmail(String email);
    Optional<User> findByUsername(String username);
    long countByRatingGreaterThan(Integer rating);
    Page<User> findAllByOrderByRatingDesc(Pageable pageable);
    List<User> findByUsernameContainingIgnoreCase(String username);
}
