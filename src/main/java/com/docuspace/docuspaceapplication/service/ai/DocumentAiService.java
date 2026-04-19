package com.docuspace.docuspaceapplication.service.ai;

import com.docuspace.docuspaceapplication.dto.ai.DocumentRewriteResponse;
import com.docuspace.docuspaceapplication.dto.ai.DocumentSummaryResponse;
import com.docuspace.docuspaceapplication.dto.ai.StructuredSummaryResult;
import com.docuspace.docuspaceapplication.entity.Document;
import com.docuspace.docuspaceapplication.repository.AiDocumentCacheRepository;
import com.docuspace.docuspaceapplication.repository.DocumentRepository;
import com.docuspace.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import tools.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class DocumentAiService {

    private final DocumentRepository documentRepository;
    private final AiDocumentCacheRepository aiDocumentCacheRepository;
    private final ChatClient chatClient;
    private final ObjectMapper objectMapper;

    @Value("${app.ai.summary.max-input-chars:12000}")
    private int maxInputChars;

    public DocumentSummaryResponse summarizeDocument(Long documentId, Long userId) {
        Document document = documentRepository.findByIdAndWorkspaceOwnerId(documentId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Document not found"));

        String title = safe(document.getTitle());
        String content = safe(document.getContent());

        if (content.isBlank()) {
            throw new IllegalArgumentException("Document content is empty");
        }

        String trimmedContent = content.length() > maxInputChars
                ? content.substring(0, maxInputChars)
                : content;

        String aiResponse = chatClient.prompt()
                .system("""
                        You are an assistant that summarizes user documents.

                        Return valid JSON only.
                        Do not include markdown fences.
                        Do not include any explanation outside JSON.
                        Do not invent facts.
                        Use only the content provided.
                        """)
                .user("""
                        Summarize the following document.

                        Return JSON in exactly this shape:
                        {
                          "overview": "short paragraph summary",
                          "keyPoints": ["point 1", "point 2", "point 3"],
                          "actionItems": ["action 1", "action 2"]
                        }

                        Rules:
                        - Keep the overview concise.
                        - Return 3 to 5 key points when possible.
                        - Return action items that are either explicitly stated or clearly implied implementation next steps.
                        - If no meaningful action items can be derived, return an empty array.
                        - Return plain JSON only.

                        Document title:
                        %s

                        Document content:
                        %s
                        """.formatted(title, trimmedContent))
                .call()
                .content();

        if (aiResponse == null || aiResponse.isBlank()) {
            throw new IllegalStateException("AI provider returned an empty summary");
        }

        StructuredSummaryResult structuredSummary;
        try {
            structuredSummary = objectMapper.readValue(aiResponse, StructuredSummaryResult.class);
        } catch (Exception ex) {
            throw new IllegalStateException("Failed to parse AI summary response", ex);
        }

        return new DocumentSummaryResponse(
                documentId,
                safe(structuredSummary.overview()),
                structuredSummary.keyPoints() != null ? structuredSummary.keyPoints() : List.of(),
                structuredSummary.actionItems() != null ? structuredSummary.actionItems() : List.of(),
                LocalDateTime.now()
        );
    }

    public DocumentRewriteResponse rewriteDocument(Long documentId, Long userId, String mode) {
        Document document = documentRepository.findByIdAndWorkspaceOwnerId(documentId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Document not found"));

        String title = safe(document.getTitle());
        String content = safe(document.getContent());

        if (content.isBlank()) {
            throw new IllegalArgumentException("Document content is empty");
        }

        String normalizedMode = normalizeRewriteMode(mode);

        String trimmedContent = content.length() > maxInputChars
                ? content.substring(0, maxInputChars)
                : content;

        String instruction = switch (normalizedMode) {
            case "improve" -> """
                    Improve the writing quality while preserving the original meaning.
                    Make it clearer, more polished, and more professional.
                    """;
            case "shorten" -> """
                    Rewrite the content to be shorter and more concise while preserving the key meaning.
                    Remove repetition and unnecessary wording.
                    """;
            case "expand" -> """
                    Rewrite the content with more detail and clarity while preserving the original meaning.
                    Expand explanations where useful, but do not invent facts.
                    """;
            case "fix_grammar" -> """
                    Fix grammar, spelling, punctuation, and sentence structure while preserving the original meaning.
                    """;
            default -> throw new IllegalArgumentException("Unsupported rewrite mode");
        };

        String rewrittenContent = chatClient.prompt()
                .system("""
                        You are an assistant that rewrites user document content.

                        Rules:
                        - Preserve the original meaning.
                        - Do not invent facts.
                        - Return only the rewritten document body.
                        - Do not include labels such as "Document title", "Revised content", "Here is the rewritten version", or similar.
                        - Do not include markdown fences.
                        - Do not include any explanation before or after the rewritten text.
                        - Do not repeat the title unless it is already part of the document body.
                        """)
                .user("""
                        Rewrite the following document content.

                        Rewrite instruction:
                        %s

                        Document title:
                        %s

                        Document content:
                        %s
                        """.formatted(instruction, title, trimmedContent))
                .call()
                .content();

        if (rewrittenContent == null || rewrittenContent.isBlank()) {
            throw new IllegalStateException("AI provider returned empty rewritten content");
        }

        String cleanedContent = sanitizeRewrittenContent(rewrittenContent);

        return new DocumentRewriteResponse(
                documentId,
                normalizedMode,
                cleanedContent,
                LocalDateTime.now()
        );
    }

    public Document loadOwnedDocument(Long documentId, Long userId) {
        return documentRepository.findByIdAndWorkspaceOwnerId(documentId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Document not found"));
    }
    public String normalizeRewriteMode(String mode) {
        if (mode == null || mode.isBlank()) {
            throw new IllegalArgumentException("Rewrite mode is required");
        }

        String normalizedMode = mode.trim().toLowerCase();

        return switch (normalizedMode) {
            case "improve", "shorten", "expand", "fix_grammar" -> normalizedMode;
            default -> throw new IllegalArgumentException("Unsupported rewrite mode");
        };

    }

    private String sanitizeRewrittenContent(String content) {
        if (content == null) {
            return "";
        }

        String cleaned = content.trim();

        cleaned = cleaned.replaceFirst("(?i)^document\\s+title\\s*:\\s*.*?\\R+", "");
        cleaned = cleaned.replaceFirst("(?i)^title\\s*:\\s*.*?\\R+", "");
        cleaned = cleaned.replaceFirst("(?i)^revised\\s+content\\s*:\\s*", "");
        cleaned = cleaned.replaceFirst("(?i)^rewritten\\s+content\\s*:\\s*", "");
        cleaned = cleaned.replaceFirst("(?i)^here\\s+is\\s+the\\s+rewritten\\s+version\\s*:?\\s*", "");

        return cleaned.trim();
    }

    private String safe(String value) {
        return value == null ? "" : value;
    }
}