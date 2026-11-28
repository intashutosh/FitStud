package com.fitstud.backend.controller;

import com.fitstud.backend.dto.routinefolder.*;
import com.fitstud.backend.service.RoutineFolderService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/routine-folders")
public class RoutineFolderController {

    private final RoutineFolderService folderService;

    public RoutineFolderController(RoutineFolderService folderService) {
        this.folderService = folderService;
    }

    private UUID getUserId(UserDetails userDetails) {
        return UUID.fromString(userDetails.getUsername());
    }

    @PostMapping
    public ResponseEntity<RoutineFolderResponse> createFolder(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody CreateRoutineFolderRequest request
    ) {
        RoutineFolderResponse response = folderService.createFolder(
                getUserId(userDetails),
                request.name()
        );

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    public ResponseEntity<List<RoutineFolderResponse>> getMyFolders(
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        return ResponseEntity.ok(
                folderService.getMyFolders(getUserId(userDetails))
        );
    }

    @PutMapping("/{folderId}")
    public ResponseEntity<RoutineFolderResponse> renameFolder(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable UUID folderId,
            @Valid @RequestBody UpdateRoutineFolderRequest request
    ) {
        return ResponseEntity.ok(
                folderService.renameFolder(
                        getUserId(userDetails),
                        folderId,
                        request.name()
                )
        );
    }

    @DeleteMapping("/{folderId}")
    public ResponseEntity<Void> deleteFolder(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable UUID folderId
    ) {
        folderService.deleteFolder(getUserId(userDetails), folderId);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/routines/{routineId}")
    public ResponseEntity<Void> moveRoutine(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable UUID routineId,
            @Valid @RequestBody MoveRoutineRequest request
    ) {
        folderService.moveRoutine(
                getUserId(userDetails),
                routineId,
                request.folderId()
        );

        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/routines/{routineId}")
    public ResponseEntity<Void> removeRoutineFromFolder(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable UUID routineId
    ) {
        folderService.removeRoutineFromFolder(
                getUserId(userDetails),
                routineId
        );

        return ResponseEntity.noContent().build();
    }
}