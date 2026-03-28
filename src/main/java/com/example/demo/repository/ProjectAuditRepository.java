package com.example.demo.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.demo.model.ProjectAudit;

public interface ProjectAuditRepository extends JpaRepository<ProjectAudit, Long> {
}
