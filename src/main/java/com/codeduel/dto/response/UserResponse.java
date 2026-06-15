package com.codeduel.dto.response;

import com.codeduel.entity.Role;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class UserResponse {
    private Long id;
    private String username;
    private String email;
    private Integer eloRating;
    private Integer wins;
    private Integer losses;
    private Integer totalMatches;
    private String avatarUrl;
    private Role role;
    private LocalDateTime createdAt;
}
