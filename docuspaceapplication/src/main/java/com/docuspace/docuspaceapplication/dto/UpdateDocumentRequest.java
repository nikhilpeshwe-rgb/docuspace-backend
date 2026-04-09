package com.docuspace.docuspaceapplication.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class UpdateDocumentRequest {

    @NotBlank(message = "Document title is required")
    private String title;

    private String content;
}