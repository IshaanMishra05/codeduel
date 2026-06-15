package com.codeduel.service;

import com.codeduel.dto.response.LeaderboardEntry;
import com.codeduel.dto.response.UserResponse;
import com.codeduel.entity.User;
import com.codeduel.exception.ResourceNotFoundException;
import com.codeduel.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    public UserResponse getProfile(Long id) {
        return toResponse(findById(id));
    }

    @Transactional(readOnly = true)
    public UserResponse getMyProfile(User user) {
        return toResponse(user);
    }

    @Transactional(readOnly = true)
    public List<LeaderboardEntry> getLeaderboard(int limit) {
        List<User> users = userRepository.findLeaderboard(PageRequest.of(0, limit));
        AtomicInteger rank = new AtomicInteger(1);
        return users.stream()
                .map(u -> {
                    double winRate = u.getTotalMatches() > 0
                            ? (double) u.getWins() / u.getTotalMatches() * 100
                            : 0.0;
                    return LeaderboardEntry.builder()
                            .rank(rank.getAndIncrement())
                            .userId(u.getId())
                            .username(u.getUsername())
                            .avatarUrl(u.getAvatarUrl())
                            .eloRating(u.getEloRating())
                            .wins(u.getWins())
                            .losses(u.getLosses())
                            .totalMatches(u.getTotalMatches())
                            .winRate(Math.round(winRate * 10.0) / 10.0)
                            .build();
                })
                .collect(Collectors.toList());
    }

    public User findById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + id));
    }

    private UserResponse toResponse(User user) {
        return UserResponse.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .eloRating(user.getEloRating())
                .wins(user.getWins())
                .losses(user.getLosses())
                .totalMatches(user.getTotalMatches())
                .avatarUrl(user.getAvatarUrl())
                .role(user.getRole())
                .createdAt(user.getCreatedAt())
                .build();
    }
}
