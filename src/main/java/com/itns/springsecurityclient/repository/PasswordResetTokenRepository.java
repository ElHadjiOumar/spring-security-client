package com.itns.springsecurityclient.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.itns.springsecurityclient.entity.PasswordResetToken;

// A JPA repository. It is a Spring Data interface for generic CRUD operations on a repository for a
// specific type. It extends the `JpaRepository` interface.
@Repository
public interface PasswordResetTokenRepository extends
        JpaRepository<PasswordResetToken,Long> {
    PasswordResetToken findByToken(String token);
}
