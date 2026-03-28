package com.example.demo.service;

import java.time.Instant;
import java.util.Objects;

import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.demo.model.Project;
import com.example.demo.model.ProjectAudit;
import com.example.demo.repository.ProjectAuditRepository;
import com.example.demo.repository.ProjectRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ProjectService {

    private final ProjectRepository projectRepository;
    private final ProjectAuditRepository projectAuditRepository;

    // このアノテーションでトランザクションにするんだろう。
    @Transactional
    // project と audit を更新する。こっちは２件の更新をするケース。
    // 引数で、id, name, description, version を受け取る。それでアップデートするのだろう。
    public Project updateWithAudit(Long id, String name, String description, Long version) {

        // まず、idでプロジェクトを取得
        var project = projectRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("project not found: " + id));

        // @Version は永続化層が採番・増分する。クライアントの version は「更新前に読んだ値」と一致するかの検証に使う。
        // data.sql 等で version 列が NULL の行があると Hibernate 7 で NPE になるため、NULL は 0 扱いに正規化する。
        long persisted = project.getVersion() != null ? project.getVersion() : 0L;
        if (!Objects.equals(persisted, version)) {
            throw new ObjectOptimisticLockingFailureException(Project.class, id);
        }
        if (project.getVersion() == null) {
            project.setVersion(0L);
        }
        project.setName(name);
        project.setDescription(description);
        var saved = projectRepository.saveAndFlush(project);

        // audit も更新
        var audit = new ProjectAudit();
        audit.setProjectId(saved.getId());
        audit.setAction("UPDATE");
        audit.setCreatedAt(Instant.now());
        projectAuditRepository.save(audit);

        return saved;
    }

    // こっちはトランザクション ロールバックの例
    // とくに問題が無くても、ロールバックするという例を示しているだけ。
    @Transactional
    public void transactionDemoRollback(Long id) {
        var project = projectRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("project not found: " + id));

        if (project.getVersion() == null) {
            project.setVersion(0L);
        }
        project.setDescription((project.getDescription() == null ? "" : project.getDescription()) + " [TX_DEMO]");
        projectRepository.save(project);

        var audit = new ProjectAudit();
        audit.setProjectId(project.getId());
        audit.setAction("TX_DEMO");
        audit.setCreatedAt(Instant.now());
        projectAuditRepository.save(audit);

        throw new IllegalStateException("transaction demo rollback");
    }
}
