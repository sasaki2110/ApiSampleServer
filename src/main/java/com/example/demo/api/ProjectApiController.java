package com.example.demo.api;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.api.project.ProjectCreateRequest;
import com.example.demo.api.project.ProjectResponse;
import com.example.demo.model.Project;
import com.example.demo.repository.ProjectRepository;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/projects")
@RequiredArgsConstructor
public class ProjectApiController {

    // 自動生成されたRepository実装がDIされる（newは不要）。
    private final ProjectRepository repository;

    @GetMapping
    public List<Project> findAll() {
        // Repositoryの標準メソッド。Controllerに定義したため外部APIとして公開される。
        return repository.findAll();
    }

    @PostMapping
    public ResponseEntity<ProjectResponse> create(@Valid @RequestBody ProjectCreateRequest req) {
        // save()は「新規作成/更新」を兼ねるが、ここはID未設定のため新規作成になる。
        var project = new Project();
        project.setName(req.name());
        project.setDescription(req.description());
        var saved = repository.save(project);
        var body = new ProjectResponse(saved.getId(), saved.getName(), saved.getDescription());
        return ResponseEntity.status(HttpStatus.CREATED).body(body);
    }
}
