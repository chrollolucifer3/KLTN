package com.learning_forum.repository;

import com.learning_forum.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, String> {
    boolean existsUserByUsername(String username);
    boolean existsUserByPhone(String phone);
    boolean existsUserByEmail(String email);
    boolean existsUserById(String id);
    Optional<User> findByUsername(String username);

}
