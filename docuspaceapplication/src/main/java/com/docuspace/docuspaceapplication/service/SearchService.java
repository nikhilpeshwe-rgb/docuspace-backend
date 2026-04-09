package com.docuspace.docuspaceapplication.service;

import com.docuspace.docuspaceapplication.dto.SearchResponse;
import com.docuspace.docuspaceapplication.entity.Document;
import com.docuspace.docuspaceapplication.entity.User;
import com.docuspace.docuspaceapplication.repository.DocumentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class SearchService {

    private final DocumentRepository documentRepository;
    private final AuthenticatedUserService authenticatedUserService;

    public List<SearchResponse> search(String query) {
        if (query == null || query.trim().isEmpty()) {
            return List.of();
        }
        User currentUser = authenticatedUserService.getCurrentUser();

        List<Document> documents = documentRepository.searchDocuments(currentUser.getId(), query);

        return documents.stream()
                .map(doc -> SearchResponse.builder()
                        .documentId(doc.getId())
                        .title(doc.getTitle())
                        .snippet(generateSnippet(doc.getContent(), query))
                        .workspaceId(doc.getWorkspace().getId())
                        .collectionId(doc.getCollection().getId())
                        .build())
                .toList();
    }

    private String generateSnippet(String content, String query) {
        if (content == null) return "";

        String lowerContent = content.toLowerCase();
        String lowerQuery = query.toLowerCase();

        int index = lowerContent.indexOf(lowerQuery);

        if (index == -1) {
            return content.substring(0, Math.min(100, content.length()));
        }

        int start = Math.max(0, index - 30);
        int end = Math.min(content.length(), index + 70);

        return content.substring(start, end) + "...";
    }
}
