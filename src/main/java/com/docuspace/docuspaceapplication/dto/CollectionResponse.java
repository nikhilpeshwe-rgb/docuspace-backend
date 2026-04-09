package com.docuspace.docuspaceapplication.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
public class CollectionResponse {
    private Long id;
    private String name;
    private Long workspaceId;
    private LocalDateTime createdAt;
}