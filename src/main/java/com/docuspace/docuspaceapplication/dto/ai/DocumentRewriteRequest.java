package com.docuspace.docuspaceapplication.dto.ai;

import jakarta.validation.constraints.NotBlank;

public record DocumentRewriteRequest(
        @NotBlank(message = "Rewrite mode is required")
        String mode
) {
}