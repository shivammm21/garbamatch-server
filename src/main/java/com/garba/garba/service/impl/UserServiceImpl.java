package com.garba.garba.service.impl;

import com.garba.garba.dto.LoginRequest;
import com.garba.garba.dto.UpdateProfileRequest;
import com.garba.garba.dto.UserCreateRequest;
import com.garba.garba.dto.UserResponse;
import com.garba.garba.dto.UserStatsResponse;
import com.garba.garba.entity.User;
import com.garba.garba.entity.UserImage;
import com.garba.garba.repository.UserRepository;
import com.garba.garba.repository.UserImageRepository;
import com.garba.garba.service.UserService;
import com.garba.garba.util.JwtUtil;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Base64;
import java.util.Optional;

@Service
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final UserImageRepository userImageRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    public UserServiceImpl(UserRepository userRepository, UserImageRepository userImageRepository, 
                          PasswordEncoder passwordEncoder, JwtUtil jwtUtil) {
        this.userRepository = userRepository;
        this.userImageRepository = userImageRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
    }

    @Override
    @Transactional
    public UserResponse register(UserCreateRequest request) {
        // Check for existing email and mobile
        boolean emailExists = userRepository.existsByEmailIgnoreCase(request.getEmail());
        boolean mobileExists = userRepository.existsByMobile(request.getMobile());
        
        if (emailExists && mobileExists) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, 
                "Email '" + request.getEmail() + "' and mobile number '" + request.getMobile() + "' already exist");
        } else if (emailExists) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, 
                "Email '" + request.getEmail() + "' already exists");
        } else if (mobileExists) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, 
                "Mobile number '" + request.getMobile() + "' already exists");
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
                .bio(request.getBio())
                .planMode("trial")
                .build();

        User saved = userRepository.save(user);

        // Handle profile picture if provided
        if (request.getProfilePicture() != null && !request.getProfilePicture().trim().isEmpty()) {
            try {
                byte[] imageData = Base64.getDecoder().decode(request.getProfilePicture());
                
                UserImage profileImage = UserImage.builder()
                        .userId(saved.getId())
                        .emailId(saved.getEmail())
                        .photoType("profile")
                        .image(imageData)
                        .build();
                
                UserImage savedImage = userImageRepository.save(profileImage);
                
                // Update user with profile pic reference
                saved.setProfilePicId(savedImage.getSrn());
                saved = userRepository.save(saved);
                
            } catch (IllegalArgumentException e) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid profile picture format");
            }
        }

        return toResponse(saved);
    }

    @Override
    @Transactional
    public UserResponse registerWithFile(UserCreateRequest request, MultipartFile profilePic) {
        // Check for existing email and mobile
        boolean emailExists = userRepository.existsByEmailIgnoreCase(request.getEmail());
        boolean mobileExists = userRepository.existsByMobile(request.getMobile());
        
        if (emailExists && mobileExists) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, 
                "Email '" + request.getEmail() + "' and mobile number '" + request.getMobile() + "' already exist");
        } else if (emailExists) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, 
                "Email '" + request.getEmail() + "' already exists");
        } else if (mobileExists) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, 
                "Mobile number '" + request.getMobile() + "' already exists");
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
                .bio(request.getBio())
                .planMode("basic")
                .build();

        User saved = userRepository.save(user);

        // Handle profile picture file if provided
        if (profilePic != null && !profilePic.isEmpty()) {
            try {
                byte[] imageData = profilePic.getBytes();
                
                UserImage profileImage = UserImage.builder()
                        .userId(saved.getId())
                        .emailId(saved.getEmail())
                        .photoType("profile")
                        .image(imageData)
                        .build();
                
                UserImage savedImage = userImageRepository.save(profileImage);
                
                // Update user with profile pic reference
                saved.setProfilePicId(savedImage.getSrn());
                saved = userRepository.save(saved);
                
            } catch (IOException e) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Error processing profile picture");
            }
        }

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

        // Generate JWT token
        String token = jwtUtil.generateToken(user.getId(), user.getEmail());
        return toResponseWithToken(user, token);
    }

    @Override
    @Transactional(readOnly = true)
    public byte[] getProfilePicture(Long imageId) {
        UserImage userImage = userImageRepository.findById(imageId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Profile picture not found"));
        return userImage.getImage();
    }

    @Override
    @Transactional(readOnly = true)
    public UserResponse getUserProfile(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
        return toResponse(user);
    }

    @Override
    @Transactional
    public UserResponse updateProfile(Long userId, UpdateProfileRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        // Update fields if provided
        if (request.getFullName() != null && !request.getFullName().trim().isEmpty()) {
            user.setFullName(request.getFullName().trim());
        }
        if (request.getAge() != null) {
            user.setAge(request.getAge());
        }
        if (request.getGarbaSkill() != null) {
            user.setGarbaSkill(request.getGarbaSkill());
        }
        if (request.getLocation() != null) {
            user.setLocation(request.getLocation());
        }
        if (request.getBio() != null) {
            user.setBio(request.getBio());
        }

        // Handle profile picture if provided
        if (request.getProfilePicture() != null && !request.getProfilePicture().trim().isEmpty()) {
            try {
                byte[] imageData = Base64.getDecoder().decode(request.getProfilePicture());
                
                // Update existing profile picture or create new one
                if (user.getProfilePicId() != null) {
                    // Update existing profile picture
                    Optional<UserImage> existingImageOpt = userImageRepository.findById(user.getProfilePicId());
                    if (existingImageOpt.isPresent()) {
                        UserImage existingImage = existingImageOpt.get();
                        existingImage.setImage(imageData);
                        userImageRepository.save(existingImage);
                        System.out.println("Updated existing profile picture with ID: " + user.getProfilePicId());
                    } else {
                        // Profile pic ID exists but record not found, create new one
                        UserImage profileImage = UserImage.builder()
                                .userId(user.getId())
                                .emailId(user.getEmail())
                                .photoType("profile")
                                .image(imageData)
                                .build();
                        
                        UserImage savedImage = userImageRepository.save(profileImage);
                        user.setProfilePicId(savedImage.getSrn());
                        System.out.println("Created new profile picture with ID: " + savedImage.getSrn());
                    }
                } else {
                    // No existing profile picture, create new one
                    UserImage profileImage = UserImage.builder()
                            .userId(user.getId())
                            .emailId(user.getEmail())
                            .photoType("profile")
                            .image(imageData)
                            .build();
                    
                    UserImage savedImage = userImageRepository.save(profileImage);
                    user.setProfilePicId(savedImage.getSrn());
                    System.out.println("Created first profile picture with ID: " + savedImage.getSrn());
                }
                
            } catch (IllegalArgumentException e) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid profile picture format");
            } catch (Exception e) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Error updating profile picture: " + e.getMessage());
            }
        }

        User savedUser = userRepository.save(user);
        return toResponse(savedUser);
    }

    @Override
    @Transactional
    public UserResponse updateProfileWithFile(Long userId, UpdateProfileRequest request, MultipartFile profilePic) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        // Update fields if provided
        if (request.getFullName() != null && !request.getFullName().trim().isEmpty()) {
            user.setFullName(request.getFullName().trim());
        }
        if (request.getAge() != null) {
            user.setAge(request.getAge());
        }
        if (request.getGarbaSkill() != null) {
            user.setGarbaSkill(request.getGarbaSkill());
        }
        if (request.getLocation() != null) {
            user.setLocation(request.getLocation());
        }
        if (request.getBio() != null) {
            user.setBio(request.getBio());
        }

        // Handle profile picture file if provided
        if (profilePic != null && !profilePic.isEmpty()) {
            try {
                System.out.println("Processing profile picture: " + profilePic.getOriginalFilename() + " (" + profilePic.getSize() + " bytes)");
                byte[] imageData = profilePic.getBytes();
                
                // Update existing profile picture or create new one
                if (user.getProfilePicId() != null) {
                    // Update existing profile picture
                    Optional<UserImage> existingImageOpt = userImageRepository.findById(user.getProfilePicId());
                    if (existingImageOpt.isPresent()) {
                        UserImage existingImage = existingImageOpt.get();
                        existingImage.setImage(imageData);
                        userImageRepository.save(existingImage);
                        System.out.println("Updated existing profile picture with ID: " + user.getProfilePicId());
                    } else {
                        // Profile pic ID exists but record not found, create new one
                        UserImage profileImage = UserImage.builder()
                                .userId(user.getId())
                                .emailId(user.getEmail())
                                .photoType("profile")
                                .image(imageData)
                                .build();
                        
                        UserImage savedImage = userImageRepository.save(profileImage);
                        user.setProfilePicId(savedImage.getSrn());
                        System.out.println("Created new profile picture with ID: " + savedImage.getSrn());
                    }
                } else {
                    // No existing profile picture, create new one
                    UserImage profileImage = UserImage.builder()
                            .userId(user.getId())
                            .emailId(user.getEmail())
                            .photoType("profile")
                            .image(imageData)
                            .build();
                    
                    UserImage savedImage = userImageRepository.save(profileImage);
                    user.setProfilePicId(savedImage.getSrn());
                    System.out.println("Created first profile picture with ID: " + savedImage.getSrn());
                }
                
            } catch (IOException e) {
                System.err.println("IOException processing profile picture: " + e.getMessage());
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Error processing profile picture: " + e.getMessage());
            } catch (Exception e) {
                System.err.println("Exception updating profile picture: " + e.getMessage());
                e.printStackTrace();
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Error updating profile picture: " + e.getMessage());
            }
        } else {
            System.out.println("No profile picture provided or file is empty");
        }

        User savedUser = userRepository.save(user);
        return toResponse(savedUser);
    }

    @Override
    @Transactional(readOnly = true)
    public UserStatsResponse getUserStats() {
        Long totalUsers = userRepository.count();
        Long trialPlanUsers = userRepository.countByPlanMode("trial");
        Long basicPlanUsers = userRepository.countByPlanMode("basic");
        Long premiumPlanUsers = userRepository.countByPlanMode("premium");
        
        return UserStatsResponse.builder()
                .totalUsers(totalUsers)
                .trialPlanUsers(trialPlanUsers)
                .basicPlanUsers(basicPlanUsers)
                .premiumPlanUsers(premiumPlanUsers)
                .build();
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
                .profilePicId(u.getProfilePicId())
                .bio(u.getBio())
                .planMode(u.getPlanMode())
                .build();
    }

    private static UserResponse toResponseWithToken(User u, String token) {
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
                .profilePicId(u.getProfilePicId())
                .bio(u.getBio())
                .planMode(u.getPlanMode())
                .token(token)
                .build();
    }
}
