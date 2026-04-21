package com.docuspace.docuspaceapplication.repository;

import com.docuspace.docuspaceapplication.entity.AiJob;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface AiJobRepository extends JpaRepository<AiJob, Long> {

    Optional<AiJob> findByIdAndUserId(Long id, Long userId);

    @Modifying
    @Query("""
    update AiJob j
    set j.status = com.docuspace.docuspaceapplication.entity.AiJobStatus.PROCESSING,
        j.updatedAt = CURRENT_TIMESTAMP
    where j.id = :jobId
      and (j.status = com.docuspace.docuspaceapplication.entity.AiJobStatus.PENDING
           or j.status = com.docuspace.docuspaceapplication.entity.AiJobStatus.FAILED)
""")
    int markProcessingIfRetryable(@Param("jobId") Long jobId);
}