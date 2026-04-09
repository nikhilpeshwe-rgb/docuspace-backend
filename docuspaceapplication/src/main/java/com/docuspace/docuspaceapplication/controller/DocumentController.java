package com.docuspace.docuspaceapplication.controller;

import com.docuspace.docuspaceapplication.dto.CreateDocumentRequest;
import com.docuspace.docuspaceapplication.dto.DocumentResponse;
import com.docuspace.docuspaceapplication.dto.DocumentVersionResponse;
import com.docuspace.docuspaceapplication.dto.UpdateDocumentRequest;
import com.docuspace.docuspaceapplication.service.DocumentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class DocumentController {

    private final DocumentService documentService;

    @PostMapping("/api/collections/{collectionId}/documents")
    public ResponseEntity<DocumentResponse> createDocument(
            @PathVariable Long collectionId,
            @Valid @RequestBody CreateDocumentRequest request) {
        return ResponseEntity.ok(documentService.createDocument(collectionId, request));
    }

    @GetMapping("/api/documents/{documentId}")
    public ResponseEntity<DocumentResponse> getDocumentById(@PathVariable Long documentId) {
        return ResponseEntity.ok(documentService.getDocumentById(documentId));
    }

    @PutMapping("/api/documents/{documentId}")
    public ResponseEntity<DocumentResponse> updateDocument(
            @PathVariable Long documentId,
            @Valid @RequestBody UpdateDocumentRequest request) {
        return ResponseEntity.ok(documentService.updateDocument(documentId, request));
    }

    @GetMapping("/api/collections/{collectionId}/documents")
    public ResponseEntity<List<DocumentResponse>> getDocumentsByCollection(
            @PathVariable Long collectionId) {
        return ResponseEntity.ok(documentService.getDocumentsByCollection(collectionId));
    }

    @GetMapping("/api/documents/{documentId}/versions")
    public ResponseEntity<List<DocumentVersionResponse>> getDocumentVersions(
            @PathVariable Long documentId) {
        return ResponseEntity.ok(documentService.getDocumentVersions(documentId));
    }

    @PostMapping("/api/documents/{documentId}/restore/{versionId}")
    public ResponseEntity<DocumentResponse> restoreDocumentVersion(
            @PathVariable Long documentId,
            @PathVariable Long versionId) {
        return ResponseEntity.ok(documentService.restoreDocumentVersion(documentId, versionId));
    }
}