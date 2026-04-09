package com.docuspace.docuspaceapplication.service;

import com.docuspace.docuspaceapplication.dto.CreateDocumentRequest;
import com.docuspace.docuspaceapplication.dto.DocumentResponse;
import com.docuspace.docuspaceapplication.dto.DocumentVersionResponse;
import com.docuspace.docuspaceapplication.dto.UpdateDocumentRequest;
import com.docuspace.docuspaceapplication.entity.*;
import com.docuspace.docuspaceapplication.repository.DocumentCollectionRepository;
import com.docuspace.docuspaceapplication.repository.DocumentRepository;
import com.docuspace.docuspaceapplication.repository.DocumentVersionRepository;
import com.docuspace.docuspaceapplication.repository.WorkspaceRepository;
import com.docuspace.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class DocumentService {

    private final DocumentRepository documentRepository;
    private final DocumentCollectionRepository documentCollectionRepository;
    private final WorkspaceRepository workspaceRepository;
    private final DocumentVersionRepository documentVersionRepository;
    private final AuthenticatedUserService authenticatedUserService;

    public DocumentResponse createDocument(Long collectionId, CreateDocumentRequest request) {
        User currentUser = authenticatedUserService.getCurrentUser();

        DocumentCollection collection = documentCollectionRepository.findById(collectionId)
                .orElseThrow(() -> new RuntimeException("Collection not found"));

        Workspace workspace = workspaceRepository
                .findByIdAndOwner_IdAndIsDeletedFalse(collection.getWorkspace().getId(), currentUser.getId())
                .orElseThrow(() -> new RuntimeException("Workspace not found or access denied"));

        Document document = Document.builder()
                .workspace(workspace)
                .collection(collection)
                .title(request.getTitle())
                .content(request.getContent())
                .createdBy(currentUser)
                .updatedBy(currentUser)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        Document savedDocument = documentRepository.save(document);
        return mapToResponse(savedDocument);
    }

    public DocumentResponse getDocumentById(Long documentId) {
        User currentUser = authenticatedUserService.getCurrentUser();

        Document document = documentRepository.findById(documentId)
                .orElseThrow(() -> new ResourceNotFoundException("Document not found"));

        workspaceRepository.findByIdAndOwner_IdAndIsDeletedFalse(
                document.getWorkspace().getId(),
                currentUser.getId()
        ).orElseThrow(() -> new RuntimeException("Access denied"));

        return mapToResponse(document);
    }

    public DocumentResponse updateDocument(Long documentId, UpdateDocumentRequest request) {
        User currentUser = authenticatedUserService.getCurrentUser();

        Document document = documentRepository.findById(documentId)
                .orElseThrow(() -> new ResourceNotFoundException("Document not found"));

        workspaceRepository.findByIdAndOwner_IdAndIsDeletedFalse(
                document.getWorkspace().getId(),
                currentUser.getId()
        ).orElseThrow(() -> new ResourceNotFoundException("Access denied"));

        saveDocumentVersion(document, currentUser);

        document.setTitle(request.getTitle());
        document.setContent(request.getContent());
        document.setUpdatedBy(currentUser);
        document.setUpdatedAt(LocalDateTime.now());

        Document updatedDocument = documentRepository.save(document);
        return mapToResponse(updatedDocument);
    }

    public List<DocumentResponse> getDocumentsByCollection(Long collectionId) {
        User currentUser = authenticatedUserService.getCurrentUser();

        DocumentCollection collection = documentCollectionRepository.findById(collectionId)
                .orElseThrow(() -> new ResourceNotFoundException("Collection not found"));

        workspaceRepository.findByIdAndOwner_IdAndIsDeletedFalse(
                collection.getWorkspace().getId(),
                currentUser.getId()
        ).orElseThrow(() -> new ResourceNotFoundException("Workspace not found or access denied"));

        return documentRepository.findByCollectionId(collectionId)
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    public List<DocumentVersionResponse> getDocumentVersions(Long documentId) {
        User currentUser = authenticatedUserService.getCurrentUser();

        Document document = documentRepository.findById(documentId)
                .orElseThrow(() -> new ResourceNotFoundException("Document not found"));

        workspaceRepository.findByIdAndOwner_IdAndIsDeletedFalse(
                document.getWorkspace().getId(),
                currentUser.getId()
        ).orElseThrow(() -> new ResourceNotFoundException("Access denied"));

        return documentVersionRepository.findByDocument_IdOrderByVersionNumberDesc(documentId)
                .stream()
                .map(this::mapVersionToResponse)
                .toList();
    }

    public DocumentResponse restoreDocumentVersion(Long documentId, Long versionId) {
        User currentUser = authenticatedUserService.getCurrentUser();

        Document document = documentRepository.findById(documentId)
                .orElseThrow(() -> new ResourceNotFoundException("Document not found"));

        workspaceRepository.findByIdAndOwner_IdAndIsDeletedFalse(
                document.getWorkspace().getId(),
                currentUser.getId()
        ).orElseThrow(() -> new ResourceNotFoundException("Access denied"));

        DocumentVersion version = documentVersionRepository.findByIdAndDocument_Id(versionId, documentId)
                .orElseThrow(() -> new ResourceNotFoundException("Document version not found"));

        saveDocumentVersion(document, currentUser);

        document.setTitle(version.getTitle());
        document.setContent(version.getContent());
        document.setUpdatedBy(currentUser);
        document.setUpdatedAt(LocalDateTime.now());

        Document restoredDocument = documentRepository.save(document);
        return mapToResponse(restoredDocument);
    }

    private DocumentResponse mapToResponse(Document document) {
        return DocumentResponse.builder()
                .id(document.getId())
                .title(document.getTitle())
                .content(document.getContent())
                .workspaceId(document.getWorkspace().getId())
                .collectionId(document.getCollection().getId())
                .createdBy(document.getCreatedBy().getId())
                .updatedBy(document.getUpdatedBy().getId())
                .createdAt(document.getCreatedAt())
                .updatedAt(document.getUpdatedAt())
                .build();
    }

    private void saveDocumentVersion(Document document, User currentUser) {
        Integer nextVersionNumber = documentVersionRepository
                .findTopByDocument_IdOrderByVersionNumberDesc(document.getId())
                .map(version -> version.getVersionNumber() + 1)
                .orElse(1);

        DocumentVersion documentVersion = DocumentVersion.builder()
                .document(document)
                .title(document.getTitle())
                .content(document.getContent())
                .versionNumber(nextVersionNumber)
                .createdBy(currentUser)
                .createdAt(LocalDateTime.now())
                .build();

        documentVersionRepository.save(documentVersion);
    }

    private DocumentVersionResponse mapVersionToResponse(DocumentVersion version) {
        return DocumentVersionResponse.builder()
                .id(version.getId())
                .documentId(version.getDocument().getId())
                .title(version.getTitle())
                .content(version.getContent())
                .versionNumber(version.getVersionNumber())
                .createdBy(version.getCreatedBy().getId())
                .createdAt(version.getCreatedAt())
                .build();
    }
}