package com.example.demo.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.demo.model.Project;

// DAO相当のインターフェース。実装クラスはSpring Data JPAが起動時に自動生成する。
public interface ProjectRepository extends JpaRepository<Project, Long> {
    // これが、クエリの宣言。
    List<Project> findByNameContainingIgnoreCase(String keyword);
}
