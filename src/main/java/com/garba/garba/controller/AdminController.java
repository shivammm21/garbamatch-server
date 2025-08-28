package com.garba.garba.controller;

import com.garba.garba.dto.AdminLoginRequest;
import com.garba.garba.dto.AdminLoginResponse;
import com.garba.garba.dto.UserStatsResponse;
import com.garba.garba.service.UserService;
import com.garba.garba.util.JwtUtil;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/admin")
public class AdminController {

    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final UserService userService;
    
    // Hardcoded admin credentials
    private static final String ADMIN_USERNAME = "admin4567";
    private static final String ADMIN_PASSWORD = "admin9876";

    public AdminController(PasswordEncoder passwordEncoder, JwtUtil jwtUtil, UserService userService) {
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
        this.userService = userService;
    }

    @PostMapping("/login")
    public ResponseEntity<AdminLoginResponse> adminLogin(@Valid @RequestBody AdminLoginRequest request) {
        try {
            // Validate admin credentials
            if (!ADMIN_USERNAME.equals(request.getUsername())) {
                throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid admin credentials");
            }
            
            if (!ADMIN_PASSWORD.equals(request.getPassword())) {
                throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid admin credentials");
            }
            
            // Generate JWT token for admin (using negative ID to distinguish from regular users)
            String token = jwtUtil.generateToken(-1L, "admin@garba.com");
            
            AdminLoginResponse response = AdminLoginResponse.builder()
                    .username(ADMIN_USERNAME)
                    .role("ADMIN")
                    .token(token)
                    .message("Admin login successful")
                    .build();
            
            return ResponseEntity.ok(response);
            
        } catch (ResponseStatusException e) {
            throw e;
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Admin login failed: " + e.getMessage());
        }
    }

    @GetMapping("/users/stats")
    public ResponseEntity<UserStatsResponse> getUserStats(@RequestHeader("Authorization") String authHeader) {
        try {
            // Validate admin token
            String token = extractTokenFromHeader(authHeader);
            
            if (!jwtUtil.isTokenValid(token)) {
                throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid or expired admin token");
            }
            
            // Check if token belongs to admin (user ID should be -1)
            Long userId = jwtUtil.getUserIdFromToken(token);
            if (!userId.equals(-1L)) {
                throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Access denied. Admin privileges required");
            }
            
            UserStatsResponse stats = userService.getUserStats();
            return ResponseEntity.ok(stats);
            
        } catch (ResponseStatusException e) {
            throw e;
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to get user stats: " + e.getMessage());
        }
    }

    private String extractTokenFromHeader(String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Authorization header must start with 'Bearer '");
        }
        return authHeader.substring(7); // Remove "Bearer " prefix
    }
}