package com.example.demo.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
public class Project {
    // JPAの主キー。save()時にIDがnullなら新規、値ありなら更新として扱われる。
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 第2章ではDBテーブルの最小例として name / description のみ保持する。
    private String name;
    private String description;
}
