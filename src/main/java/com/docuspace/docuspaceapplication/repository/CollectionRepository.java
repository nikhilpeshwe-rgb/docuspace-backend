package com.docuspace.docuspaceapplication.repository;

import com.docuspace.docuspaceapplication.entity.Collection;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CollectionRepository extends JpaRepository<Collection, Long> {
    List<Collection> findByWorkspaceId(Long workspaceId);
}
