package com.codeduel.controller;

import com.codeduel.dto.response.LeaderboardEntry;
import com.codeduel.dto.response.UserResponse;
import com.codeduel.entity.User;
import com.codeduel.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    // ── Current user ────────────────────────────────────────────────────────

    @GetMapping("/api/users/me")
    public ResponseEntity<UserResponse> getMyProfile(@AuthenticationPrincipal User user) {
        return ResponseEntity.ok(userService.getMyProfile(user));
    }

    // ── Public profile by ID ─────────────────────────────────────────────────

    @GetMapping("/api/users/{id}")
    public ResponseEntity<UserResponse> getProfile(@PathVariable Long id) {
        return ResponseEntity.ok(userService.getProfile(id));
    }

    // ── Leaderboard (public) ─────────────────────────────────────────────────

    @GetMapping("/api/leaderboard")
    public ResponseEntity<List<LeaderboardEntry>> getLeaderboard(
            @RequestParam(defaultValue = "50") int limit) {
        int safeLimit = Math.min(limit, 100);
        return ResponseEntity.ok(userService.getLeaderboard(safeLimit));
    }
}
