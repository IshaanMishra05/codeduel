package com.codeduel.codeduel.repository;
import com.codeduel.codeduel.entity.User;
import jakarta.validation.constraints.NotBlank;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUsername(@NotBlank String username);

    Collection<User> findAllByOrderByEloRatingDesc();
}