package com.docuspace.docuspaceapplication.repository;

import com.docuspace.docuspaceapplication.entity.AiDocumentCache;
import com.docuspace.docuspaceapplication.entity.AiOperationType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AiDocumentCacheRepository extends JpaRepository<AiDocumentCache, Long> {

    Optional<AiDocumentCache> findByDocumentIdAndOperationTypeAndMode(
            Long documentId,
            AiOperationType operationType,
            String mode
    );
}