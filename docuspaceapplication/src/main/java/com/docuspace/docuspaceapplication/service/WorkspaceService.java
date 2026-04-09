package com.docuspace.docuspaceapplication.service;

import com.docuspace.docuspaceapplication.dto.CreateWorkspaceRequest;
import com.docuspace.docuspaceapplication.dto.WorkspaceResponse;
import com.docuspace.docuspaceapplication.entity.User;
import com.docuspace.docuspaceapplication.entity.Workspace;
import com.docuspace.docuspaceapplication.repository.WorkspaceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class WorkspaceService {
    private final WorkspaceRepository workspaceRepository;
    private final AuthenticatedUserService authenticatedUserService;

    public WorkspaceResponse createWorkspace(CreateWorkspaceRequest request) {
        User currentUser = authenticatedUserService.getCurrentUser();

        Workspace workspace = Workspace.builder()
                .name(request.getName())
                .owner(currentUser)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .description(request.getDescription())
                .isDeleted(false)
                .build();

        Workspace savedWorkspace =  workspaceRepository.save(workspace);

        return mapToResponse(savedWorkspace);
    }

    public List<WorkspaceResponse> getMyWorkspaces() {
        User currentUser = authenticatedUserService.getCurrentUser();

        return workspaceRepository.findByOwner_IdAndIsDeletedFalse(currentUser.getId())
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    private WorkspaceResponse mapToResponse(Workspace savedWorkSpace) {
        Long ownerId = savedWorkSpace.getOwner() != null ? savedWorkSpace.getOwner().getId() : null;
        return WorkspaceResponse.builder()
                .id(savedWorkSpace.getId())
                .name(savedWorkSpace.getName())
                .ownerId(ownerId)
                .createdAt(LocalDateTime.now())
                .description(savedWorkSpace.getDescription())
                .build();
    }
}
