package com.docuspace.docuspaceapplication.dto.ai;

import jakarta.validation.constraints.NotNull;

public record CreateSummaryJobRequest(
        @NotNull Long documentId
) {
}