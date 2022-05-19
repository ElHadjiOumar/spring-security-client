package com.itns.springsecurityclient.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.itns.springsecurityclient.entity.User;
import org.springframework.stereotype.Repository;

// A JPA repository. It is a Spring Data interface for generic CRUD operations on a repository for a
// specific type. It extends the `JpaRepository` interface.
@Repository
public interface UserRepository  extends JpaRepository<User,Long> {
    User findByEmail(String email);
}