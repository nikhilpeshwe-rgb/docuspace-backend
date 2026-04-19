package com.docuspace.docuspaceapplication.dto.ai;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record CreateRewriteJobRequest(
        @NotNull Long documentId,
        @NotBlank String mode
) {
}