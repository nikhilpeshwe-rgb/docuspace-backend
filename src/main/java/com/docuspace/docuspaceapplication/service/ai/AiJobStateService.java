package com.docuspace.docuspaceapplication.service.ai;

import com.docuspace.docuspaceapplication.entity.AiJob;
import com.docuspace.docuspaceapplication.entity.AiJobStatus;
import com.docuspace.docuspaceapplication.repository.AiJobRepository;
import com.docuspace.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class AiJobStateService {

    private final AiJobRepository aiJobRepository;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public boolean markProcessingIfRetryable(Long jobId) {
        int updated = aiJobRepository.markProcessingIfRetryable(jobId);

        if (updated > 0) {
            log.info("Marked AI job as PROCESSING. jobId={}", jobId);
            return true;
        }

        log.info("Did not mark AI job as PROCESSING because it is not retryable. jobId={}", jobId);
        return false;
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void markFailed(Long jobId, String errorMessage) {
        AiJob job = aiJobRepository.findById(jobId)
                .orElseThrow(() -> new ResourceNotFoundException("AI job not found"));

        job.setStatus(AiJobStatus.FAILED);
        job.setCompletedAt(null);
        job.setErrorMessage(errorMessage);
        job.setUpdatedAt(LocalDateTime.now());

        aiJobRepository.save(job);

        log.info("Marked AI job as FAILED. jobId={}, error={}", jobId, errorMessage);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void markCompleted(Long jobId, String resultJson) {
        AiJob job = aiJobRepository.findById(jobId)
                .orElseThrow(() -> new ResourceNotFoundException("AI job not found"));

        job.setResultJson(resultJson);
        job.setStatus(AiJobStatus.COMPLETED);
        job.setCompletedAt(LocalDateTime.now());
        job.setUpdatedAt(LocalDateTime.now());
        job.setErrorMessage(null);

        aiJobRepository.save(job);

        log.info("Marked AI job as COMPLETED. jobId={}", jobId);
    }
}