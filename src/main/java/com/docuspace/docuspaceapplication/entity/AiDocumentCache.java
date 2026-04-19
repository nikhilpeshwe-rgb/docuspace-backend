package com.docuspace.docuspaceapplication.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "ai_document_cache",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_ai_cache_document_operation_mode",
                        columnNames = {"document_id", "operation_type", "mode"}
                )
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AiDocumentCache {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "document_id", nullable = false)
    private Long documentId;

    @Enumerated(EnumType.STRING)
    @Column(name = "operation_type", nullable = false, length = 20)
    private AiOperationType operationType;

    @Column(name = "mode", length = 50)
    private String mode;

    @Column(name = "content_hash", nullable = false, length = 64)
    private String contentHash;

    @Column(name = "result_json", nullable = false, columnDefinition = "TEXT")
    private String resultJson;

    @Column(name = "generated_at", nullable = false)
    private LocalDateTime generatedAt;
}