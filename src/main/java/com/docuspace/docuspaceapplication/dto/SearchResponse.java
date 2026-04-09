package com.docuspace.docuspaceapplication.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@AllArgsConstructor
public class SearchResponse {

    private Long documentId;
    private String title;
    private String snippet;
    private Long workspaceId;
    private Long collectionId;
}