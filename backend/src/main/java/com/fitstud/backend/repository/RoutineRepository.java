package com.fitstud.backend.repository;

import com.fitstud.backend.entity.Routine;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface RoutineRepository extends JpaRepository<Routine, UUID> {

    // Retrieve all routines belonging to a user, newest first.
    List<Routine> findByUser_IdOrderByCreatedAtDesc(UUID userId);

    // Retrieve a routine only if it belongs to the specified user.
    Optional<Routine> findByIdAndUser_Id(UUID routineId, UUID userId);

    // Check whether a user owns a particular routine name.
    boolean existsByUser_IdAndNameIgnoreCase(UUID userId, String name);
}