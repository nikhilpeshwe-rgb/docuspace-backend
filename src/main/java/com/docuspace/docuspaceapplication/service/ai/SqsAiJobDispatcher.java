package com.docuspace.docuspaceapplication.service.ai;

import com.docuspace.docuspaceapplication.dto.ai.AiJobMessage;
import io.awspring.cloud.sqs.operations.SqsTemplate;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class SqsAiJobDispatcher implements AiJobDispatcher {

    private final SqsTemplate sqsTemplate;

    @Value("${app.ai.jobs.queue-name}")
    private String queueName;

    @Override
    public void dispatch(Long jobId) {
        AiJobMessage message = new AiJobMessage(jobId);
        log.info("Sending AI job message to SQS. jobId={}, queue={}", jobId, queueName);
        sqsTemplate.send(to -> to.queue(queueName).payload(message));
        log.info("Dispatched AI job to SQS. jobId={}, queue={}", jobId, queueName);
    }
}