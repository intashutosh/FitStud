
package com.fitstud.backend.repository;

import com.fitstud.backend.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface UserRepository extends JpaRepository<User, UUID> {

    Optional<User> findByEmail(String email);

    boolean existsByEmail(String email);

    boolean existsByUsernameIgnoreCase(String username);
    Optional<User> findByUsernameIgnoreCase(String username);
}
