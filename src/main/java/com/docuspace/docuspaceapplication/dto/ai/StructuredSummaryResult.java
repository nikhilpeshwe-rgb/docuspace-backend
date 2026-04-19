package com.docuspace.docuspaceapplication.dto.ai;

import java.util.List;

public record StructuredSummaryResult(
        String overview,
        List<String> keyPoints,
        List<String> actionItems
) {
}