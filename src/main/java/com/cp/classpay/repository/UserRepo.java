package com.cp.classpay.repository;

import com.cp.classpay.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;


@Repository
public interface UserRepo extends JpaRepository<User, Long> {
    Optional<User> findUserByEmail(String email);
    default User findByEmail(String email) {
        return findUserByEmail(email).orElseThrow(() -> new IllegalArgumentException("User not found by email: " + email));
    }
}
