package com.fitstud.backend.service;

import com.fitstud.backend.dto.routinefolder.*;
import com.fitstud.backend.entity.Routine;
import com.fitstud.backend.entity.RoutineFolder;
import com.fitstud.backend.entity.User;
import com.fitstud.backend.exception.ResourceNotFoundException;
import com.fitstud.backend.repository.RoutineFolderRepository;
import com.fitstud.backend.repository.RoutineRepository;
import com.fitstud.backend.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class RoutineFolderService {

    private final RoutineFolderRepository folderRepository;
    private final RoutineRepository routineRepository;
    private final UserRepository userRepository;

    public RoutineFolderService(
            RoutineFolderRepository folderRepository,
            RoutineRepository routineRepository,
            UserRepository userRepository
    ) {
        this.folderRepository = folderRepository;
        this.routineRepository = routineRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    public RoutineFolderResponse createFolder(UUID userId, String name) {
        String normalizedName = normalizeName(name);

        if (folderRepository.existsByUser_IdAndNameIgnoreCase(
                userId, normalizedName
        )) {
            throw new IllegalStateException(
                    "A folder with this name already exists"
            );
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() ->
                        new ResourceNotFoundException("User not found")
                );

        List<RoutineFolder> folders =
                folderRepository.findByUser_IdOrderByPositionAsc(userId);

        RoutineFolder folder = RoutineFolder.create(
                user,
                normalizedName,
                folders.size()
        );

        RoutineFolder saved = folderRepository.save(folder);
        return toResponse(saved, 0);
    }

    @Transactional(readOnly = true)
    public List<RoutineFolderResponse> getMyFolders(UUID userId) {
        return folderRepository.findByUser_IdOrderByPositionAsc(userId)
                .stream()
                .map(folder -> toResponse(
                        folder,
                        routineRepository.countByUser_IdAndFolder_Id(
                                userId, folder.getId()
                        )
                ))
                .toList();
    }

    @Transactional
    public RoutineFolderResponse renameFolder(
            UUID userId,
            UUID folderId,
            String name
    ) {
        RoutineFolder folder = findOwnedFolder(folderId, userId);
        String normalizedName = normalizeName(name);

        if (folderRepository.existsByUser_IdAndNameIgnoreCaseAndIdNot(
                userId, normalizedName, folderId
        )) {
            throw new IllegalStateException(
                    "A folder with this name already exists"
            );
        }

        folder.setName(normalizedName);

        return toResponse(
                folder,
                routineRepository.countByUser_IdAndFolder_Id(
                        userId, folderId
                )
        );
    }

    @Transactional
    public void deleteFolder(UUID userId, UUID folderId) {
        RoutineFolder folder = findOwnedFolder(folderId, userId);

        // The database FK uses ON DELETE SET NULL.
        folderRepository.delete(folder);
    }

    @Transactional
    public void moveRoutine(
            UUID userId,
            UUID routineId,
            UUID folderId
    ) {
        Routine routine = findOwnedRoutine(routineId, userId);
        RoutineFolder folder = findOwnedFolder(folderId, userId);

        routine.setFolder(folder);
    }

    @Transactional
    public void removeRoutineFromFolder(
            UUID userId,
            UUID routineId
    ) {
        Routine routine = findOwnedRoutine(routineId, userId);
        routine.setFolder(null);
    }

    private RoutineFolder findOwnedFolder(UUID folderId, UUID userId) {
        return folderRepository.findByIdAndUser_Id(folderId, userId)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Folder not found")
                );
    }

    private Routine findOwnedRoutine(UUID routineId, UUID userId) {
        return routineRepository.findByIdAndUser_Id(routineId, userId)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Routine not found")
                );
    }

    private String normalizeName(String name) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException(
                    "Folder name cannot be blank"
            );
        }

        String normalized = name.trim();

        if (normalized.length() > 100) {
            throw new IllegalArgumentException(
                    "Folder name cannot exceed 100 characters"
            );
        }

        return normalized;
    }

    private RoutineFolderResponse toResponse(
            RoutineFolder folder,
            long routineCount
    ) {
        return new RoutineFolderResponse(
                folder.getId(),
                folder.getName(),
                folder.getPosition(),
                routineCount,
                folder.getCreatedAt(),
                folder.getUpdatedAt()
        );
    }
}