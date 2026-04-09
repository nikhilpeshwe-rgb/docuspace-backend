package com.docuspace.docuspaceapplication.service;

import com.docuspace.docuspaceapplication.dto.CollectionResponse;
import com.docuspace.docuspaceapplication.dto.CreateCollectionRequest;
import com.docuspace.docuspaceapplication.entity.DocumentCollection;
import com.docuspace.docuspaceapplication.entity.User;
import com.docuspace.docuspaceapplication.entity.Workspace;
import com.docuspace.docuspaceapplication.repository.DocumentCollectionRepository;
import com.docuspace.docuspaceapplication.repository.WorkspaceRepository;
import com.docuspace.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CollectionService {

    private final DocumentCollectionRepository documentCollectionRepository;
    private final WorkspaceRepository workspaceRepository;
    private final AuthenticatedUserService authenticatedUserService;

    public CollectionResponse createCollection(Long workspaceId, CreateCollectionRequest request) {
        User currentUser = authenticatedUserService.getCurrentUser();

        Workspace workspace = workspaceRepository
                .findByIdAndOwner_IdAndIsDeletedFalse(workspaceId, currentUser.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Workspace not found or access denied"));

        DocumentCollection collection = DocumentCollection.builder()
                .workspace(workspace)
                .name(request.getName())
                .createdAt(LocalDateTime.now())
                .build();

        DocumentCollection savedCollection = documentCollectionRepository.save(collection);

        return mapToResponse(savedCollection);
    }

    public List<CollectionResponse> getCollectionsByWorkspace(Long workspaceId) {
        User currentUser = authenticatedUserService.getCurrentUser();

        Workspace workspace = workspaceRepository
                .findByIdAndOwner_IdAndIsDeletedFalse(workspaceId, currentUser.getId())
                .orElseThrow(() -> new RuntimeException("Workspace not found or access denied"));

        return documentCollectionRepository.findByWorkspace_Id(workspace.getId())
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    private CollectionResponse mapToResponse(DocumentCollection collection) {
        return CollectionResponse.builder()
                .id(collection.getId())
                .name(collection.getName())
                .workspaceId(collection.getWorkspace().getId())
                .createdAt(collection.getCreatedAt())
                .build();
    }
}