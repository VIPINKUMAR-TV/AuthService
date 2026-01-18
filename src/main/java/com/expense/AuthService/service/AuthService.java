package com.expense.AuthService.service;

import lombok.RequiredArgsConstructor;

import java.util.HashMap;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestBody;

import com.expense.AuthService.DTO.LoginRequest;
import com.expense.AuthService.DTO.RegisterRequest;
import com.expense.AuthService.model.User;
import com.expense.AuthService.repository.UserRepository;
import com.expense.AuthService.security.JwtUtil;

@Service
@RequiredArgsConstructor
public class AuthService {
	private final UserRepository repo;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    public ResponseEntity<Map<String, String>> register(@RequestBody RegisterRequest request) {
        if (repo.existsByEmail(request.getEmail())) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Email already exists!");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        }

        User user = User.builder()
                .username(request.getUsername())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .role("USER")
                .build();

        repo.save(user);
        
     // Return a JSON response
        Map<String, String> response = new HashMap<>();
        response.put("message", "User registered successfully!");
        return ResponseEntity.ok(response);
    }

    public String login(LoginRequest request) {
        User user = repo.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new RuntimeException("Invalid credentials!");
        }

        
        return jwtUtil.generateToken(user);
    }
}
