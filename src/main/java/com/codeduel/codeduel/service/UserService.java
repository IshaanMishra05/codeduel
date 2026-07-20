package com.codeduel.codeduel.service;

import com.codeduel.codeduel.dto.request.UserRequest;
import com.codeduel.codeduel.dto.response.UserResponse;
import com.codeduel.codeduel.entity.Role;
import com.codeduel.codeduel.entity.User;
import com.codeduel.codeduel.repository.UserRepository;
import com.codeduel.codeduel.security.JwtService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import com.codeduel.codeduel.dto.request.RegisterRequest;
import com.codeduel.codeduel.dto.request.LoginRequest;
import com.codeduel.codeduel.dto.response.AuthResponse;

import java.util.List;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder, JwtService jwtService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    public AuthResponse register(RegisterRequest request) {
        User user = new User();
        user.setUsername(request.getUsername());
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setRole(Role.PLAYER);
        userRepository.save(user);

        String token = jwtService.generateToken(user.getUsername());
        return new AuthResponse(token);
    }

    public AuthResponse login(LoginRequest request) {
        User user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new RuntimeException("Invalid username or password"));

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new RuntimeException("Invalid username or password");
        }

        String token = jwtService.generateToken(user.getUsername());
        return new AuthResponse(token);
    }


    public List<UserResponse> getAll() {
        return userRepository.findAll().stream()
                .map(this::toUserResponse)
                .toList();
    }

    public UserResponse getById(Long id) {
        return userRepository.findById(id)
                .map(this::toUserResponse)
                .orElse(null);
    }

    public UserResponse create(UserRequest request) {
        User user = new User();
        user.setUsername(request.getUsername());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setEmail(request.getEmail());
        user.setRole(Role.PLAYER);
        userRepository.save(user);
        return toUserResponse(user);
    }

    public void delete(Long id) {
        userRepository.deleteById(id);
    }

    private UserResponse toUserResponse(User user) {
        UserResponse response = new UserResponse();
        response.setId(user.getId());
        response.setUsername(user.getUsername());
        response.setEmail(user.getEmail());
        response.setEloRating(user.getEloRating());
        response.setWins(user.getWins());
        response.setLosses(user.getLosses());
        response.setRole(user.getRole().name());
        return response;
    }

    public void updateElo(Long winnerId, Long loserId) {
        User winner = userRepository.findById(winnerId)
                .orElseThrow(() -> new RuntimeException("Winner not found"));
        User loser = userRepository.findById(loserId)
                .orElseThrow(() -> new RuntimeException("Loser not found"));

        double expectedWinner = 1.0 / (1 + Math.pow(10,
                (loser.getEloRating() - winner.getEloRating()) / 400.0));
        double expectedLoser = 1.0 - expectedWinner;

        int K = 32;

        int newWinnerRating = (int) Math.round(winner.getEloRating() + K * (1 - expectedWinner));
        int newLoserRating  = (int) Math.round(loser.getEloRating()  + K * (0 - expectedLoser));

        winner.setEloRating(newWinnerRating);
        winner.setWins(winner.getWins() + 1);
        winner.setTotalMatches(winner.getTotalMatches() + 1);

        loser.setEloRating(newLoserRating);
        loser.setLosses(loser.getLosses() + 1);
        loser.setTotalMatches(loser.getTotalMatches() + 1);

        userRepository.save(winner);
        userRepository.save(loser);
    }

    public List<UserResponse> getLeaderboard() {
        return userRepository.findAllByOrderByEloRatingDesc()
                .stream()
                .map(this::toUserResponse)
                .toList();
    }
}
