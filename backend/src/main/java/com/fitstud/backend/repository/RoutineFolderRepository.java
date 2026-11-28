
package com.fitstud.backend.repository;

import com.fitstud.backend.entity.RoutineFolder;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface RoutineFolderRepository
        extends JpaRepository<RoutineFolder, UUID> {

    List<RoutineFolder> findByUser_IdOrderByPositionAsc(
            UUID userId
    );

    Optional<RoutineFolder> findByIdAndUser_Id(
            UUID folderId,
            UUID userId
    );

    boolean existsByUser_IdAndNameIgnoreCase(
            UUID userId,
            String name
    );

    boolean existsByUser_IdAndNameIgnoreCaseAndIdNot(
            UUID userId,
            String name,
            UUID folderId
    );
}
