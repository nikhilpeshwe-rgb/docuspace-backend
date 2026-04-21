package com.docuspace.docuspaceapplication.service.ai;

import com.docuspace.docuspaceapplication.dto.ai.AiJobCreatedResponse;
import com.docuspace.docuspaceapplication.dto.ai.AiJobStatusResponse;
import com.docuspace.docuspaceapplication.dto.ai.DocumentRewriteResponse;
import com.docuspace.docuspaceapplication.dto.ai.DocumentSummaryResponse;
import com.docuspace.docuspaceapplication.entity.AiJob;
import com.docuspace.docuspaceapplication.entity.AiJobStatus;
import com.docuspace.docuspaceapplication.entity.AiOperationType;
import com.docuspace.docuspaceapplication.entity.Document;
import com.docuspace.docuspaceapplication.repository.AiJobRepository;
import com.docuspace.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class AiJobService {

    private static final String DEFAULT_MODE = "default";

    private final AiJobRepository aiJobRepository;
    private final DocumentAiService documentAiService;
    private final AiDocumentCacheService aiDocumentCacheService;
    private final AiJobProcessor aiJobProcessor;
    private final ObjectMapper objectMapper;
    private final AiJobDispatcher aiJobDispatcher;

    public AiJobCreatedResponse createSummaryJob(Long documentId, Long userId) {
        log.info("Creating summary AI job. documentId={}, userId={}", documentId, userId);
        Document document = documentAiService.loadOwnedDocument(documentId, userId);

        String contentHash = aiDocumentCacheService.buildContentHash(
                document.getTitle(),
                document.getContent()
        );

        DocumentSummaryResponse cachedResponse =
                aiDocumentCacheService.getCachedSummary(documentId, contentHash);

        AiJob job = new AiJob();
        job.setDocumentId(documentId);
        job.setUserId(userId);
        job.setOperationType(AiOperationType.SUMMARY);
        job.setMode(DEFAULT_MODE);
        job.setContentHash(contentHash);
        job.setCreatedAt(LocalDateTime.now());
        job.setUpdatedAt(LocalDateTime.now());

        if (cachedResponse != null) {
            log.info("Summary AI cache hit. documentId={}, userId={}, contentHash={}",
                    documentId, userId, contentHash);
            job.setStatus(AiJobStatus.COMPLETED);
            job.setCompletedAt(LocalDateTime.now());
            job.setResultJson(writeJson(cachedResponse));
            aiJobRepository.save(job);

            return new AiJobCreatedResponse(job.getId(), job.getStatus().name());
        }

        job.setStatus(AiJobStatus.PENDING);
        aiJobRepository.save(job);
        log.info("Created summary AI job. jobId={}, documentId={}, userId={}, status={}",
                job.getId(), documentId, userId, job.getStatus());

//        try {
//            aiJobProcessor.processSummaryJobAsync(job.getId());
//        } catch (Exception ex) {
//            job.setStatus(AiJobStatus.FAILED);
//            job.setErrorMessage("System is busy. Please try again.");
//            job.setUpdatedAt(LocalDateTime.now());
//            aiJobRepository.save(job);
//        }

        try {
            log.info("Dispatching summary AI job to queue. jobId={}, documentId={}",
                    job.getId(), documentId);
            aiJobDispatcher.dispatch(job.getId());
        } catch (Exception ex) {
            log.error("Failed to dispatch summary AI job. jobId={}", job.getId(), ex);
            job.setStatus(AiJobStatus.FAILED);
            job.setErrorMessage("System is busy. Please try again.");
            job.setUpdatedAt(LocalDateTime.now());
            aiJobRepository.save(job);
        }

        return new AiJobCreatedResponse(job.getId(), job.getStatus().name());
    }

    public AiJobCreatedResponse createRewriteJob(Long documentId, Long userId, String mode) {

        String normalizedMode = documentAiService.normalizeRewriteMode(mode);
        log.info("Creating rewrite AI job. documentId={}, userId={}, mode={}",
                documentId, userId, normalizedMode);

        Document document = documentAiService.loadOwnedDocument(documentId, userId);

        String contentHash = aiDocumentCacheService.buildContentHash(
                document.getTitle(),
                document.getContent()
        );

        DocumentRewriteResponse cachedResponse =
                aiDocumentCacheService.getCachedRewrite(documentId, normalizedMode, contentHash);

        AiJob job = new AiJob();
        job.setDocumentId(documentId);
        job.setUserId(userId);
        job.setOperationType(AiOperationType.REWRITE);
        job.setMode(normalizedMode);
        job.setContentHash(contentHash);
        job.setCreatedAt(LocalDateTime.now());
        job.setUpdatedAt(LocalDateTime.now());

        if (cachedResponse != null) {
            log.info("Rewrite AI cache hit. documentId={}, userId={}, mode={}, contentHash={}",
                    documentId, userId, normalizedMode, contentHash);
            job.setStatus(AiJobStatus.COMPLETED);
            job.setCompletedAt(LocalDateTime.now());
            job.setResultJson(writeJson(cachedResponse));
            aiJobRepository.save(job);

            return new AiJobCreatedResponse(job.getId(), job.getStatus().name());
        }

        job.setStatus(AiJobStatus.PENDING);
        aiJobRepository.save(job);
        log.info("Created rewrite AI job. jobId={}, documentId={}, userId={}, mode={}, status={}",
                job.getId(), documentId, userId, normalizedMode, job.getStatus());

//        try {
//            aiJobProcessor.processRewriteJobAsync(job.getId());
//        } catch (Exception ex) {
//            job.setStatus(AiJobStatus.FAILED);
//            job.setErrorMessage("System is busy. Please try again.");
//            job.setUpdatedAt(LocalDateTime.now());
//            aiJobRepository.save(job);
//        }

        try {
            log.info("Dispatching rewrite AI job to queue. jobId={}, documentId={}, mode={}",
                    job.getId(), documentId, normalizedMode);
            aiJobDispatcher.dispatch(job.getId());
        } catch (Exception ex) {
            log.error("Failed to dispatch rewrite AI job. jobId={}", job.getId(), ex);
            job.setStatus(AiJobStatus.FAILED);
            job.setErrorMessage("System is busy. Please try again.");
            job.setUpdatedAt(LocalDateTime.now());
            aiJobRepository.save(job);
        }

        return new AiJobCreatedResponse(job.getId(), job.getStatus().name());
    }

    public AiJobStatusResponse getJobStatus(Long jobId, Long userId) {
        AiJob job = aiJobRepository.findByIdAndUserId(jobId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("AI job not found"));

        JsonNode result = null;

        if (job.getResultJson() != null && !job.getResultJson().isBlank()) {
            try {
                result = objectMapper.readTree(job.getResultJson());
            } catch (Exception ex) {
                throw new IllegalStateException("Failed to parse AI job result", ex);
            }
        }

        return new AiJobStatusResponse(
                job.getId(),
                job.getStatus().name(),
                job.getOperationType(),
                result,
                job.getErrorMessage()
        );
    }

    private String writeJson(Object value) {
        try {
            return objectMapper.writeValueAsString(value);
        } catch (Exception ex) {
            throw new IllegalStateException("Failed to serialize AI job result", ex);
        }
    }
}