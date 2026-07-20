package com.codeduel.codeduel.dto.response;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserResponse {
    private Long id;
    private String username;
    private String email;
    private Integer eloRating;
    private Integer wins;
    private Integer losses;
    private String role;
}