package com.garba.garba.repository;

import com.garba.garba.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    boolean existsByEmailIgnoreCase(String email);
    boolean existsByMobile(String mobile);

    Optional<User> findByEmailIgnoreCase(String email);
    Optional<User> findByMobile(String mobile);
}
