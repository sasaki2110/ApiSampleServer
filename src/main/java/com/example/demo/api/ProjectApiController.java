package com.example.demo.api;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.api.project.ProjectCreateRequest;
import com.example.demo.api.project.ProjectResponse;
import com.example.demo.api.project.ProjectUpdateRequest;
import com.example.demo.model.Project;
import com.example.demo.repository.ProjectRepository;
import com.example.demo.service.ProjectService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/projects")
@RequiredArgsConstructor
public class ProjectApiController {

    private final ProjectRepository repository;
    private final ProjectService projectService;

    @GetMapping
    public List<Project> findAll() {
        return repository.findAll();
    }

    @GetMapping("/{id}")
    public ProjectResponse findOne(@PathVariable Long id) {
        var p = repository.findById(id).orElseThrow(() -> new IllegalArgumentException("project not found: " + id));
        return new ProjectResponse(p.getId(), p.getVersion(), p.getName(), p.getDescription());
    }

    @GetMapping("/search")
    public List<ProjectResponse> search(@RequestParam String keyword) {
        return repository.findByNameContainingIgnoreCase(keyword)
            .stream()
            .map(p -> new ProjectResponse(p.getId(), p.getVersion(), p.getName(), p.getDescription()))
            .toList();
    }

    @PostMapping
    public ResponseEntity<ProjectResponse> create(@Valid @RequestBody ProjectCreateRequest req) {
        var project = new Project();
        project.setName(req.name());
        project.setDescription(req.description());
        var saved = repository.save(project);
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(new ProjectResponse(saved.getId(), saved.getVersion(), saved.getName(), saved.getDescription()));
    }

    @PutMapping("/{id}")
    public ProjectResponse update(@PathVariable Long id, @Valid @RequestBody ProjectUpdateRequest req) {
        var saved = projectService.updateWithAudit(id, req.name(), req.description(), req.version());
        return new ProjectResponse(saved.getId(), saved.getVersion(), saved.getName(), saved.getDescription());
    }

    @PostMapping("/{id}/tx-demo")
    public ResponseEntity<Void> txDemo(@PathVariable Long id) {
        projectService.transactionDemoRollback(id);
        return ResponseEntity.noContent().build();
    }
}
