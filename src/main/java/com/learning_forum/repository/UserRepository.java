package com.learning_forum.repository;

import com.learning_forum.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends JpaRepository<User, String> {
    boolean existsUserByUsername(String username);
    boolean existsUserByPhone(String phone);
    boolean existsUserByEmail(String email);
    boolean existsUserById(String id);
}
