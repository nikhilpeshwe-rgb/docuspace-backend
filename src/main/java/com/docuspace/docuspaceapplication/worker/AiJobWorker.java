package com.docuspace.docuspaceapplication.worker;

import com.docuspace.docuspaceapplication.dto.ai.AiJobMessage;
import com.docuspace.docuspaceapplication.service.ai.AiJobProcessor;
import io.awspring.cloud.sqs.annotation.SqsListener;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@Profile("worker")
@RequiredArgsConstructor
public class AiJobWorker {

    private final AiJobProcessor aiJobProcessor;

    @SqsListener("${app.ai.jobs.queue-name}")
    public void handleMessage(AiJobMessage message) throws Exception {
        long startTime = System.currentTimeMillis();
        log.info("Received AI job message. jobId={}", message.jobId());
        try {
            aiJobProcessor.processJob(message.jobId());

            long durationMs = System.currentTimeMillis() - startTime;
            log.info("Finished AI job message handling. jobId={}, durationMs={}",
                    message.jobId(), durationMs);
        } catch (Exception ex) {
            long durationMs = System.currentTimeMillis() - startTime;
            log.error("AI job message handling failed. jobId={}, durationMs={}",
                    message.jobId(), durationMs, ex);
            throw ex;
        }
    }
}