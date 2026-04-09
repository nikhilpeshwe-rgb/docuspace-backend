package com.docuspace.docuspaceapplication.repository;

import com.docuspace.docuspaceapplication.entity.Workspace;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface WorkspaceRepository extends JpaRepository<Workspace, Long> {
    List<Workspace> findByOwnerId(Long userId);

    List<Workspace> findByOwner_IdAndIsDeletedFalse(Long ownerId);

    Optional<Workspace> findByIdAndOwner_IdAndIsDeletedFalse(Long workspaceId, Long ownerId);
}
