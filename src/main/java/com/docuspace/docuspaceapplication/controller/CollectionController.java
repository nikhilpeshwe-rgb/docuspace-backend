package com.docuspace.docuspaceapplication.controller;

import com.docuspace.docuspaceapplication.dto.CollectionResponse;
import com.docuspace.docuspaceapplication.dto.CreateCollectionRequest;
import com.docuspace.docuspaceapplication.service.CollectionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/workspaces/{workspaceId}/collections")
@RequiredArgsConstructor
public class CollectionController {

    private final CollectionService collectionService;

    @PostMapping
    public ResponseEntity<CollectionResponse> createCollection(
            @PathVariable Long workspaceId,
            @Valid @RequestBody CreateCollectionRequest request) {
        return ResponseEntity.ok(collectionService.createCollection(workspaceId, request));
    }

    @GetMapping
    public ResponseEntity<List<CollectionResponse>> getCollectionsByWorkspace(
            @PathVariable Long workspaceId) {
        return ResponseEntity.ok(collectionService.getCollectionsByWorkspace(workspaceId));
    }
}