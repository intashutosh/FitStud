package com.fitstud.backend.repository;

import com.fitstud.backend.entity.Exercise;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ExerciseRepository extends JpaRepository<Exercise, UUID> {

    List<Exercise> findByOwnerIsNullOrderByNameAsc();

    List<Exercise> findByOwner_IdOrderByNameAsc(UUID ownerId);

    Optional<Exercise> findByIdAndOwner_Id(UUID id, UUID ownerId);

    boolean existsByOwnerIsNullAndNameIgnoreCase(String name);

    boolean existsByOwner_IdAndNameIgnoreCase(UUID ownerId, String name);
}