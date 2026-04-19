package com.docuspace.docuspaceapplication.service.ai;

import com.docuspace.docuspaceapplication.dto.ai.DocumentRewriteResponse;
import com.docuspace.docuspaceapplication.dto.ai.DocumentSummaryResponse;
import com.docuspace.docuspaceapplication.entity.AiDocumentCache;
import com.docuspace.docuspaceapplication.entity.AiOperationType;
import com.docuspace.docuspaceapplication.repository.AiDocumentCacheRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import tools.jackson.databind.ObjectMapper;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class AiDocumentCacheService {

    private final AiDocumentCacheRepository aiDocumentCacheRepository;
    private final ObjectMapper objectMapper;

    public String buildContentHash(String title, String content) {
        String input = safe(title) + "::" + safe(content);

        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hashBytes = digest.digest(input.getBytes(StandardCharsets.UTF_8));

            StringBuilder hex = new StringBuilder();
            for (byte b : hashBytes) {
                hex.append(String.format("%02x", b));
            }
            return hex.toString();
        } catch (NoSuchAlgorithmException ex) {
            throw new IllegalStateException("Failed to compute content hash", ex);
        }
    }

    public DocumentSummaryResponse getCachedSummary(Long documentId, String contentHash) {
        return getCachedResult(
                documentId,
                AiOperationType.SUMMARY,
                normalizeCacheMode(null),
                contentHash,
                DocumentSummaryResponse.class
        );
    }

    public void saveSummaryCache(Long documentId, String contentHash, DocumentSummaryResponse response) {
        saveOrUpdateCache(
                documentId,
                AiOperationType.SUMMARY,
                normalizeCacheMode(null),
                contentHash,
                response
        );
    }

    public DocumentRewriteResponse getCachedRewrite(Long documentId, String mode, String contentHash) {
        return getCachedResult(
                documentId,
                AiOperationType.REWRITE,
                normalizeCacheMode(mode),
                contentHash,
                DocumentRewriteResponse.class
        );
    }

    public void saveRewriteCache(Long documentId, String mode, String contentHash, DocumentRewriteResponse response) {
        saveOrUpdateCache(
                documentId,
                AiOperationType.REWRITE,
                normalizeCacheMode(mode),
                contentHash,
                response
        );
    }

    private <T> T getCachedResult(
            Long documentId,
            AiOperationType operationType,
            String mode,
            String contentHash,
            Class<T> responseType
    ) {
        return aiDocumentCacheRepository
                .findByDocumentIdAndOperationTypeAndMode(documentId, operationType, mode)
                .filter(cache -> cache.getContentHash().equals(contentHash))
                .map(cache -> {
                    try {
                        return objectMapper.readValue(cache.getResultJson(), responseType);
                    } catch (Exception ex) {
                        log.warn(
                                "Deleting invalid cached AI result for documentId={}, operationType={}, mode={}",
                                documentId,
                                operationType,
                                mode,
                                ex
                        );
                        aiDocumentCacheRepository.delete(cache);
                        return null;
                    }
                })
                .orElse(null);
    }

    private void saveOrUpdateCache(
            Long documentId,
            AiOperationType operationType,
            String mode,
            String contentHash,
            Object result
    ) {
        String resultJson;
        try {
            resultJson = objectMapper.writeValueAsString(result);
        } catch (Exception ex) {
            throw new IllegalStateException("Failed to serialize AI result", ex);
        }

        AiDocumentCache cache = aiDocumentCacheRepository
                .findByDocumentIdAndOperationTypeAndMode(documentId, operationType, mode)
                .orElseGet(AiDocumentCache::new);

        cache.setDocumentId(documentId);
        cache.setOperationType(operationType);
        cache.setMode(mode);
        cache.setContentHash(contentHash);
        cache.setResultJson(resultJson);
        cache.setGeneratedAt(LocalDateTime.now());

        aiDocumentCacheRepository.save(cache);
    }

    private String normalizeCacheMode(String mode) {
        return (mode == null || mode.isBlank()) ? "default" : mode.trim().toLowerCase();
    }

    private String safe(String value) {
        return value == null ? "" : value;
    }
}
