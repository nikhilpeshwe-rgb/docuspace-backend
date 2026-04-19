package com.docuspace.docuspaceapplication.dto.ai;

import com.docuspace.docuspaceapplication.entity.AiOperationType;
import tools.jackson.databind.JsonNode;

public record AiJobStatusResponse(
        Long jobId,
        String status,
        AiOperationType operationType,
        JsonNode result,
        String errorMessage
) {
}