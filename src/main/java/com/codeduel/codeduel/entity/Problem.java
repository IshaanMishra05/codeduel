package com.codeduel.codeduel.entity;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "problems")
@Data
public class Problem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String description;

    private String difficulty;
    private String language;

    @ManyToOne
    @JoinColumn(name = "created_by")
    private User createdBy;
}