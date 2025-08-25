package com.garba.garba.service.impl;

import com.garba.garba.dto.LoginRequest;
import com.garba.garba.dto.UserCreateRequest;
import com.garba.garba.dto.UserResponse;
import com.garba.garba.entity.User;
import com.garba.garba.repository.UserRepository;
import com.garba.garba.service.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.Optional;

@Service
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserServiceImpl(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional
    public UserResponse register(UserCreateRequest request) {
        // unique checks
        if (userRepository.existsByEmailIgnoreCase(request.getEmail())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Email already in use");
        }
        if (userRepository.existsByMobile(request.getMobile())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Mobile number already in use");
        }

        String hash = passwordEncoder.encode(request.getPassword());

        User user = User.builder()
                .fullName(request.getFullName())
                .email(request.getEmail().trim())
                .mobile(request.getMobile().trim())
                .passwordHash(hash)
                .age(request.getAge())
                .gender(request.getGender())
                .garbaSkill(request.getGarbaSkill())
                .location(request.getLocation())
                .build();

        User saved = userRepository.save(user);
        return toResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public UserResponse login(LoginRequest request) {
        String identifier = request.getIdentifier().trim();
        Optional<User> userOpt;
        if (identifier.contains("@")) {
            userOpt = userRepository.findByEmailIgnoreCase(identifier);
        } else {
            userOpt = userRepository.findByMobile(identifier);
        }

        User user = userOpt.orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid credentials"));

        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid credentials");
        }

        return toResponse(user);
    }

    private static UserResponse toResponse(User u) {
        return UserResponse.builder()
                .id(u.getId())
                .fullName(u.getFullName())
                .email(u.getEmail())
                .mobile(u.getMobile())
                .age(u.getAge())
                .gender(u.getGender())
                .garbaSkill(u.getGarbaSkill())
                .location(u.getLocation())
                .walletAmount(u.getWalletAmount())
                .build();
    }
}
