package com.docuspace.docuspaceapplication.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
public class DocumentResponse {
    private Long id;
    private String title;
    private String content;
    private Long workspaceId;
    private Long collectionId;
    private Long createdBy;
    private Long updatedBy;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}