package com.docuspace.docuspaceapplication.controller;

import com.docuspace.docuspaceapplication.dto.ai.AiJobCreatedResponse;
import com.docuspace.docuspaceapplication.dto.ai.AiJobStatusResponse;
import com.docuspace.docuspaceapplication.dto.ai.CreateRewriteJobRequest;
import com.docuspace.docuspaceapplication.dto.ai.CreateSummaryJobRequest;
import com.docuspace.docuspaceapplication.entity.User;
import com.docuspace.docuspaceapplication.service.ai.AiJobService;
import com.docuspace.docuspaceapplication.service.AuthenticatedUserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/ai/jobs")
@RequiredArgsConstructor
public class AiJobController {

    private final AiJobService aiJobService;
    private final AuthenticatedUserService authenticatedUserService;

    @PostMapping("/summarize")
    public AiJobCreatedResponse createSummaryJob(
            @Valid @RequestBody CreateSummaryJobRequest request
    ) {
        User currentUser = authenticatedUserService.getCurrentUser();
        return aiJobService.createSummaryJob(request.documentId(), currentUser.getId());
    }

    @GetMapping("/{jobId}")
    public AiJobStatusResponse getJobStatus(@PathVariable Long jobId) {
        User currentUser = authenticatedUserService.getCurrentUser();
        return aiJobService.getJobStatus(jobId, currentUser.getId());
    }

    @PostMapping("/rewrite")
    public AiJobCreatedResponse createRewriteJob(
            @Valid @RequestBody CreateRewriteJobRequest request
    ) {
        User currentUser = authenticatedUserService.getCurrentUser();
        return aiJobService.createRewriteJob(
                request.documentId(),
                currentUser.getId(),
                request.mode()
        );
    }
}