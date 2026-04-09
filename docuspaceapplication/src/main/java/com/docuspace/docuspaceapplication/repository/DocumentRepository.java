package com.docuspace.docuspaceapplication.repository;

import com.docuspace.docuspaceapplication.entity.Document;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface DocumentRepository extends JpaRepository<Document, Long> {
    List<Document> findByWorkspaceId(Long workspaceId);
    List<Document> findByCollectionId(Long collectionId);
    List<Document> findByCreatedById(Long createdById);
    Optional<Document> findByIdAndWorkspace_Id(Long documentId, Long workspaceId);

    @Query("""
    SELECT d FROM Document d
    WHERE d.workspace.owner.id = :userId
      AND (
            LOWER(d.title) LIKE LOWER(CONCAT('%', :query, '%'))
         OR LOWER(d.content) LIKE LOWER(CONCAT('%', :query, '%'))
      )
""")
    List<Document> searchDocuments(Long userId, String query);
}
