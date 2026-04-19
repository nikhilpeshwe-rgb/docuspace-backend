package com.docuspace.docuspaceapplication.dto.ai;
import java.time.LocalDateTime;
import java.util.List;

public record DocumentSummaryResponse(
        Long documentId,
        String overview,
        List<String> keyPoints,
        List<String> actionItems,
        LocalDateTime generatedAt
) {
}