package com.docuspace.docuspaceapplication.repository;

import com.docuspace.docuspaceapplication.entity.DocumentCollection;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface DocumentCollectionRepository extends JpaRepository<DocumentCollection, Long> {
    List<DocumentCollection> findByWorkspace_Id(Long workspaceId);

    Optional<DocumentCollection> findByIdAndWorkspace_Id(Long documentId, Long workspaceId);
}