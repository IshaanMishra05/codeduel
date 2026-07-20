package com.codeduel.codeduel.entity;

import com.codeduel.codeduel.entity.Role;
import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Entity
@Table(name = "users")
@Data
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String username;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String password;

    private Integer eloRating = 1200;

    private Integer wins = 0;

    private Integer losses = 0;

    private Integer totalMatches = 0;

    private String avatarUrl;

    private Boolean isActive = true;

    @Enumerated(EnumType.STRING)
    private Role role = Role.PLAYER;

    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}