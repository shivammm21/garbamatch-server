package com.garba.garba.entity;

import jakarta.persistence.*;
import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "user_images")
public class UserImage {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long srn;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "email_id", nullable = false, length = 180)
    private String emailId;

    @Column(name = "photo_type", nullable = false, length = 20)
    private String photoType; // "profile" or "dash"

    @Lob
    @Column(name = "image", columnDefinition = "LONGBLOB")
    private byte[] image;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", insertable = false, updatable = false)
    private User user;
}