package com.docuspace.docuspaceapplication.dto.ai;

import java.time.LocalDateTime;

public record DocumentRewriteResponse(
        Long documentId,
        String mode,
        String content,
        LocalDateTime generatedAt
) {
}