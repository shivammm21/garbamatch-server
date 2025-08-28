package com.garba.garba.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "users", indexes = {
        @Index(name = "idx_users_email", columnList = "email", unique = true),
        @Index(name = "idx_users_mobile", columnList = "mobile", unique = true)
})
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "full_name", nullable = false, length = 120)
    private String fullName;

    @Column(nullable = false, length = 180, unique = true)
    private String email;

    @Column(nullable = false, length = 20, unique = true)
    private String mobile;

    @Column(name = "password_hash", nullable = false, length = 100)
    private String passwordHash;

    private Integer age; // optional

    @Column(length = 20)
    private String gender; // e.g., Male/Female/Other

    @Column(name = "garba_skill", length = 50)
    private String garbaSkill; // e.g., Beginner/Intermediate/Expert

    @Column(length = 120)
    private String location;

    @Column(name = "wallet_amount", precision = 12, scale = 2, nullable = false)
    private BigDecimal walletAmount;

    @Column(name = "profile_pic_id")
    private Long profilePicId; // Reference to UserImage table

    @Column(length = 500)
    private String bio; // User biography/description

    @Column(name = "plan_mode", length = 50, nullable = false)
    private String planMode; // User plan type: basic, premium, etc.

    @PrePersist
    public void prePersist() {
        if (walletAmount == null) {
            walletAmount = BigDecimal.ZERO;
        }
        if (planMode == null || planMode.trim().isEmpty()) {
            planMode = "basic";
        }
    }
}
