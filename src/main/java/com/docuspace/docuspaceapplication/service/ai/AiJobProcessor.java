package com.docuspace.docuspaceapplication.service.ai;

import com.docuspace.docuspaceapplication.dto.ai.DocumentRewriteResponse;
import com.docuspace.docuspaceapplication.dto.ai.DocumentSummaryResponse;
import com.docuspace.docuspaceapplication.entity.AiJob;
import com.docuspace.docuspaceapplication.entity.AiJobStatus;
import com.docuspace.docuspaceapplication.entity.AiOperationType;
import com.docuspace.docuspaceapplication.entity.Document;
import com.docuspace.docuspaceapplication.repository.AiJobRepository;
import com.docuspace.exception.ResourceNotFoundException;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import tools.jackson.databind.ObjectMapper;

import java.time.LocalDateTime;

//@Slf4j
//@Service
//@RequiredArgsConstructor
//public class AiJobProcessor {
//
//    private final AiJobRepository aiJobRepository;
//    private final DocumentAiService documentAiService;
//    private final AiDocumentCacheService aiDocumentCacheService;
//    private final ObjectMapper objectMapper;
//
//    @Async("aiJobExecutor")
//    public void processSummaryJobAsync(Long jobId) {
//        AiJob job = null;
//
//        try {
//            job = aiJobRepository.findById(jobId)
//                    .orElseThrow(() -> new ResourceNotFoundException("AI job not found"));
//
//            log.info("Processing summary AI job asynchronously. jobId={}, thread={}",
//                    jobId, Thread.currentThread().getName());
//
//            job.setStatus(AiJobStatus.PROCESSING);
//            job.setUpdatedAt(LocalDateTime.now());
//            aiJobRepository.save(job);
//
//            Document document = documentAiService.loadOwnedDocument(job.getDocumentId(), job.getUserId());
//            String contentHash = aiDocumentCacheService.buildContentHash(
//                    document.getTitle(),
//                    document.getContent()
//            );
//
//            DocumentSummaryResponse response =
//                    documentAiService.summarizeDocument(job.getDocumentId(), job.getUserId());
//
//            aiDocumentCacheService.saveSummaryCache(job.getDocumentId(), contentHash, response);
//
//            job.setResultJson(objectMapper.writeValueAsString(response));
//            job.setStatus(AiJobStatus.COMPLETED);
//            job.setCompletedAt(LocalDateTime.now());
//            job.setUpdatedAt(LocalDateTime.now());
//            job.setErrorMessage(null);
//
//            aiJobRepository.save(job);
//        } catch (Exception ex) {
//            log.error("Failed to process summary AI job. jobId={}", jobId, ex);
//
//            if (job != null) {
//                job.setStatus(AiJobStatus.FAILED);
//                job.setCompletedAt(null);
//                job.setErrorMessage(buildSafeErrorMessage(ex));
//                job.setUpdatedAt(LocalDateTime.now());
//                aiJobRepository.save(job);
//            }
//        }
//    }
//
//    @Async("aiJobExecutor")
//    public void processRewriteJobAsync(Long jobId) {
//        AiJob job = null;
//
//        try {
//            job = aiJobRepository.findById(jobId)
//                    .orElseThrow(() -> new ResourceNotFoundException("AI job not found"));
//
//            log.info("Processing rewrite AI job asynchronously. jobId={}, thread={}",
//                    jobId, Thread.currentThread().getName());
//
//            job.setStatus(AiJobStatus.PROCESSING);
//            job.setUpdatedAt(LocalDateTime.now());
//            aiJobRepository.save(job);
//
//            Document document = documentAiService.loadOwnedDocument(job.getDocumentId(), job.getUserId());
//
//            String contentHash = aiDocumentCacheService.buildContentHash(
//                    document.getTitle(),
//                    document.getContent()
//            );
//
//            DocumentRewriteResponse response =
//                    documentAiService.rewriteDocument(
//                            job.getDocumentId(),
//                            job.getUserId(),
//                            job.getMode()
//                    );
//
//            aiDocumentCacheService.saveRewriteCache(
//                    job.getDocumentId(),
//                    job.getMode(),
//                    contentHash,
//                    response
//            );
//
//            job.setResultJson(objectMapper.writeValueAsString(response));
//            job.setStatus(AiJobStatus.COMPLETED);
//            job.setCompletedAt(LocalDateTime.now());
//            job.setUpdatedAt(LocalDateTime.now());
//            job.setErrorMessage(null);
//
//            aiJobRepository.save(job);
//        } catch (Exception ex) {
//            log.error("Failed to process rewrite AI job. jobId={}", jobId, ex);
//
//            if (job != null) {
//                job.setStatus(AiJobStatus.FAILED);
//                job.setCompletedAt(null);
//                job.setErrorMessage(buildSafeErrorMessage(ex));
//                job.setUpdatedAt(LocalDateTime.now());
//                aiJobRepository.save(job);
//            }
//        }
//    }
//
//    private String buildSafeErrorMessage(Exception ex) {
//        if (ex instanceof IllegalArgumentException) {
//            return ex.getMessage();
//        }
//        return "Failed to process AI job";
//    }
//}

@Slf4j
@Service
@RequiredArgsConstructor
public class AiJobProcessor {

    private final AiJobRepository aiJobRepository;
    private final DocumentAiService documentAiService;
    private final AiDocumentCacheService aiDocumentCacheService;
    private final AiJobStateService aiJobStateService;
    private final ObjectMapper objectMapper;

    public void processJob(Long jobId) throws Exception {
        long totalStartTime = System.currentTimeMillis();

        log.info("Starting AI job processing. jobId={}", jobId);

        boolean claimed = aiJobStateService.markProcessingIfRetryable(jobId);

        if (!claimed) {
            log.info("Skipping AI job because it is already claimed or processed. jobId={}", jobId);
            return;
        }

        AiJob job = aiJobRepository.findById(jobId)
                .orElseThrow(() -> new ResourceNotFoundException("AI job not found"));

        log.info("Claimed AI job successfully. jobId={}, documentId={}, userId={}, operationType={}, mode={}",
                job.getId(), job.getDocumentId(), job.getUserId(), job.getOperationType(), job.getMode());

        try {
            String resultJson;

            if (job.getOperationType() == AiOperationType.SUMMARY) {
                resultJson = processSummaryJob(job);
            } else if (job.getOperationType() == AiOperationType.REWRITE) {
                resultJson = processRewriteJob(job);
            } else {
                throw new IllegalStateException("Unsupported AI operation type");
            }

            aiJobStateService.markCompleted(jobId, resultJson);

            long totalDurationMs = System.currentTimeMillis() - totalStartTime;
            log.info("Completed AI job successfully. jobId={}, operationType={}, durationMs={}",
                    jobId, job.getOperationType(), totalDurationMs);

        } catch (Exception ex) {
            aiJobStateService.markFailed(jobId, buildSafeErrorMessage(ex));

            long totalDurationMs = System.currentTimeMillis() - totalStartTime;
            log.error("Failed AI job. jobId={}, operationType={}, durationMs={}, error={}",
                    jobId,
                    job.getOperationType(),
                    totalDurationMs,
                    buildSafeErrorMessage(ex),
                    ex);

            throw ex;
        }
    }

    private String processSummaryJob(AiJob job) throws Exception {
        long startTime = System.currentTimeMillis();

        log.info("Generating summary for AI job. jobId={}, documentId={}",
                job.getId(), job.getDocumentId());

        Document document = documentAiService.loadOwnedDocument(job.getDocumentId(), job.getUserId());

        String contentHash = aiDocumentCacheService.buildContentHash(
                document.getTitle(),
                document.getContent()
        );

        DocumentSummaryResponse response =
                documentAiService.summarizeDocument(job.getDocumentId(), job.getUserId());

        aiDocumentCacheService.saveSummaryCache(job.getDocumentId(), contentHash, response);

        long durationMs = System.currentTimeMillis() - startTime;
        log.info("Generated summary for AI job. jobId={}, documentId={}, durationMs={}",
                job.getId(), job.getDocumentId(), durationMs);

        return objectMapper.writeValueAsString(response);
    }

    private String processRewriteJob(AiJob job) throws Exception {
        long startTime = System.currentTimeMillis();

        log.info("Generating rewrite for AI job. jobId={}, documentId={}, mode={}",
                job.getId(), job.getDocumentId(), job.getMode());

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

        long durationMs = System.currentTimeMillis() - startTime;
        log.info("Generated rewrite for AI job. jobId={}, documentId={}, mode={}, durationMs={}",
                job.getId(), job.getDocumentId(), job.getMode(), durationMs);

        return objectMapper.writeValueAsString(response);
    }

    private String buildSafeErrorMessage(Exception ex) {
        if (ex instanceof IllegalArgumentException) {
            return ex.getMessage();
        }
        return "Failed to process AI job";
    }
}
