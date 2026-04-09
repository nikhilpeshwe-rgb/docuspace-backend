package com.docuspace.docuspaceapplication.controller;

import com.docuspace.docuspaceapplication.dto.CreateWorkspaceRequest;
import com.docuspace.docuspaceapplication.dto.WorkspaceResponse;
import com.docuspace.docuspaceapplication.service.WorkspaceService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("api/workspaces")
@RequiredArgsConstructor
public class WorkspaceController {

    private final WorkspaceService workspaceService;

    @PostMapping
    public ResponseEntity<WorkspaceResponse> createWorkspace(
            @Valid @RequestBody CreateWorkspaceRequest request){
        return ResponseEntity.ok(workspaceService.createWorkspace(request));
    }

    @GetMapping
    public ResponseEntity<List<WorkspaceResponse>> getMyWorkspaces() {
        return ResponseEntity.ok(workspaceService.getMyWorkspaces());
    }
}
