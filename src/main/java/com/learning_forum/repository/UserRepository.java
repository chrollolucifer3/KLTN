package com.learning_forum.repository;

import com.learning_forum.domain.STATUS;
import com.learning_forum.domain.USER_ROLE;
import com.learning_forum.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.sql.Date;
import java.time.LocalDateTime;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, String>, JpaSpecificationExecutor<User> {
    boolean existsUserByUsername(String username);
    boolean existsUserByPhone(String phone);
    boolean existsUserByEmail(String email);
    boolean existsUserById(String id);
    Optional<User> findByUsername(String username);
    Optional<User> findByUsernameAndRole(String username, USER_ROLE role);
    @Query("SELECT COUNT(u) FROM User u WHERE u.createdAt BETWEEN :start AND :end AND u.isActive = false ")
    int countNewUser(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);


}
