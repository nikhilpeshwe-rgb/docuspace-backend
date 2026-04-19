package com.docuspace.docuspaceapplication.service.ai;

import com.docuspace.docuspaceapplication.dto.ai.DocumentRewriteResponse;
import com.docuspace.docuspaceapplication.dto.ai.DocumentSummaryResponse;
import com.docuspace.docuspaceapplication.entity.AiJob;
import com.docuspace.docuspaceapplication.entity.AiJobStatus;
import com.docuspace.docuspaceapplication.entity.Document;
import com.docuspace.docuspaceapplication.repository.AiJobRepository;
import com.docuspace.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import tools.jackson.databind.ObjectMapper;

import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class AiJobProcessor {

    private final AiJobRepository aiJobRepository;
    private final DocumentAiService documentAiService;
    private final AiDocumentCacheService aiDocumentCacheService;
    private final ObjectMapper objectMapper;

    @Async("aiJobExecutor")
    public void processSummaryJobAsync(Long jobId) {
        AiJob job = null;

        try {
            job = aiJobRepository.findById(jobId)
                    .orElseThrow(() -> new ResourceNotFoundException("AI job not found"));

            log.info("Processing summary AI job asynchronously. jobId={}, thread={}",
                    jobId, Thread.currentThread().getName());

            job.setStatus(AiJobStatus.PROCESSING);
            job.setUpdatedAt(LocalDateTime.now());
            aiJobRepository.save(job);

            Document document = documentAiService.loadOwnedDocument(job.getDocumentId(), job.getUserId());
            String contentHash = aiDocumentCacheService.buildContentHash(
                    document.getTitle(),
                    document.getContent()
            );

            DocumentSummaryResponse response =
                    documentAiService.summarizeDocument(job.getDocumentId(), job.getUserId());

            aiDocumentCacheService.saveSummaryCache(job.getDocumentId(), contentHash, response);

            job.setResultJson(objectMapper.writeValueAsString(response));
            job.setStatus(AiJobStatus.COMPLETED);
            job.setCompletedAt(LocalDateTime.now());
            job.setUpdatedAt(LocalDateTime.now());
            job.setErrorMessage(null);

            aiJobRepository.save(job);
        } catch (Exception ex) {
            log.error("Failed to process summary AI job. jobId={}", jobId, ex);

            if (job != null) {
                job.setStatus(AiJobStatus.FAILED);
                job.setCompletedAt(null);
                job.setErrorMessage(buildSafeErrorMessage(ex));
                job.setUpdatedAt(LocalDateTime.now());
                aiJobRepository.save(job);
            }
        }
    }

    @Async("aiJobExecutor")
    public void processRewriteJobAsync(Long jobId) {
        AiJob job = null;

        try {
            job = aiJobRepository.findById(jobId)
                    .orElseThrow(() -> new ResourceNotFoundException("AI job not found"));

            log.info("Processing rewrite AI job asynchronously. jobId={}, thread={}",
                    jobId, Thread.currentThread().getName());

            job.setStatus(AiJobStatus.PROCESSING);
            job.setUpdatedAt(LocalDateTime.now());
            aiJobRepository.save(job);

            Document document = documentAiService.loadOwnedDocument(job.getDocumentId(), job.getUserId());

            String contentHash = aiDocumentCacheService.buildContentHash(
                    document.getTitle(),
                    document.getContent()
            );

            DocumentRewriteResponse response =
                    documentAiService.rewriteDocument(
                            job.getDocumentId(),
                            job.getUserId(),
                            job.getMode()
                    );

            aiDocumentCacheService.saveRewriteCache(
                    job.getDocumentId(),
                    job.getMode(),
                    contentHash,
                    response
            );

            job.setResultJson(objectMapper.writeValueAsString(response));
            job.setStatus(AiJobStatus.COMPLETED);
            job.setCompletedAt(LocalDateTime.now());
            job.setUpdatedAt(LocalDateTime.now());
            job.setErrorMessage(null);

            aiJobRepository.save(job);
        } catch (Exception ex) {
            log.error("Failed to process rewrite AI job. jobId={}", jobId, ex);

            if (job != null) {
                job.setStatus(AiJobStatus.FAILED);
                job.setCompletedAt(null);
                job.setErrorMessage(buildSafeErrorMessage(ex));
                job.setUpdatedAt(LocalDateTime.now());
                aiJobRepository.save(job);
            }
        }
    }

    private String buildSafeErrorMessage(Exception ex) {
        if (ex instanceof IllegalArgumentException) {
            return ex.getMessage();
        }
        return "Failed to process AI job";
    }
}
