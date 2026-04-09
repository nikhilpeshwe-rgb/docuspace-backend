package com.docuspace.docuspaceapplication.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
public class DocumentVersionResponse {
    private Long id;
    private Long documentId;
    private String title;
    private String content;
    private Integer versionNumber;
    private Long createdBy;
    private LocalDateTime createdAt;
}