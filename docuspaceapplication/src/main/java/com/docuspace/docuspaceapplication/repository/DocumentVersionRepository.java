package com.docuspace.docuspaceapplication.repository;

import com.docuspace.docuspaceapplication.entity.DocumentVersion;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface DocumentVersionRepository extends JpaRepository<DocumentVersion, Long> {

    List<DocumentVersion> findByDocument_IdOrderByVersionNumberDesc(Long documentId);

    Optional<DocumentVersion> findTopByDocument_IdOrderByVersionNumberDesc(Long documentId);

    Optional<DocumentVersion> findByIdAndDocument_Id(Long versionId, Long documentId);
}