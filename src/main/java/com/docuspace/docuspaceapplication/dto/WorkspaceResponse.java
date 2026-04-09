package com.docuspace.docuspaceapplication.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
public class WorkspaceResponse {
    private Long id;
    private String name;
    private Long ownerId;
    private LocalDateTime createdAt;
    private String description;
}