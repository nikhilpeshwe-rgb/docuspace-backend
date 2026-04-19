package com.docuspace.docuspaceapplication.controller;

import com.docuspace.docuspaceapplication.dto.ai.DocumentRewriteRequest;
import com.docuspace.docuspaceapplication.dto.ai.DocumentRewriteResponse;
import com.docuspace.docuspaceapplication.dto.ai.DocumentSummaryResponse;
import com.docuspace.docuspaceapplication.entity.User;
import com.docuspace.docuspaceapplication.service.AuthenticatedUserService;
import com.docuspace.docuspaceapplication.service.ai.DocumentAiService;
import com.docuspace.docuspaceapplication.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/documents")
@RequiredArgsConstructor
public class DocumentAiController {

    private final DocumentAiService documentAiService;
    private final UserService userService;
    private final AuthenticatedUserService authenticatedUserService;

    @PostMapping("/{documentId}/summarize")
    public DocumentSummaryResponse summarizeDocument(
            @PathVariable Long documentId
    ) {
        User currentUser = authenticatedUserService.getCurrentUser();
        return documentAiService.summarizeDocument(documentId, currentUser.getId());
    }

    @PostMapping("/{documentId}/rewrite")
    public DocumentRewriteResponse rewriteDocument(
            @PathVariable Long documentId,
            @Valid @RequestBody DocumentRewriteRequest request
    ) {
        User currentUser = authenticatedUserService.getCurrentUser();
        return documentAiService.rewriteDocument(documentId, currentUser.getId(), request.mode());
    }
}
