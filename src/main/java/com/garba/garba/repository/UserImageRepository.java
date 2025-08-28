package com.garba.garba.repository;

import com.garba.garba.entity.UserImage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserImageRepository extends JpaRepository<UserImage, Long> {
    Optional<UserImage> findByUserIdAndPhotoType(Long userId, String photoType);
}