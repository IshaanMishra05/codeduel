package com.codeduel.codeduel.entity;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "match_players")
@Data
public class MatchPlayer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "match_id")
    private Match match;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    private Boolean isReady = false;
}