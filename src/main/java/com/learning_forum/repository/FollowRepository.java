package com.learning_forum.repository;

import com.learning_forum.dto.projection.UserFollowerProjection;
import com.learning_forum.entity.Follow;
import com.learning_forum.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface FollowRepository extends JpaRepository<Follow, String> {
    boolean existsByFollowerAndFollowing(User currentUser, User userToFollow);
    boolean existsByFollowerIdAndFollowingId(String followerId, String followingId);
    int countByFollowingId(String followingId);
    Optional<Follow> findByFollowerAndFollowing(User follower, User following);
    Page<Follow> findAllByFollower(User follower, Pageable pageable);

    // lấy số lượng người dùng đang theo dõi user có id là followerId
    @Query("SELECT COUNT(f) FROM Follow f WHERE f.follower.id = :followerId")
    Long countFollowingByUserId(@Param("followerId") String followerId);
    // Lấy danh sách người dùng đang theo dõi user có id là followerId với phân trang và sắp xếp
    @Query(value = """
    SELECT
        u.id AS id,
        u.username AS username,
        u.email AS email,
        u.phone AS phone,
        u.full_name AS fullName,
        u.dob AS dob,
        u.role AS role,
        u.avatar_url AS avatarUrl,
        u.is_active AS isActive,
        u.created_at AS createdAt,
        COALESCE(p.post_count, 0) AS postCount,
        COALESCE(fol.followers_count, 0) AS followersCount
    FROM user_follows f
    JOIN users u ON u.id = f.following_id
    LEFT JOIN (
        SELECT user_id, COUNT(*) AS post_count
        FROM posts
        GROUP BY user_id
    ) p ON p.user_id = u.id
    LEFT JOIN (
        SELECT following_id, COUNT(*) AS followers_count
        FROM user_follows
        GROUP BY following_id
    ) fol ON fol.following_id = u.id
    WHERE f.follower_id = :followerId
    ORDER BY
        CASE WHEN :sortBy = 'createdAt' AND :order = 'asc' THEN u.created_at END ASC,
        CASE WHEN :sortBy = 'createdAt' AND :order = 'desc' THEN u.created_at END DESC,
        CASE WHEN :sortBy = 'username' AND :order = 'asc' THEN u.username END ASC,
        CASE WHEN :sortBy = 'username' AND :order = 'desc' THEN u.username END DESC
    LIMIT :limit OFFSET :offset
""", nativeQuery = true)
    List<UserFollowerProjection> findFollowingUsersWithStatsDynamicOrder(
            @Param("followerId") String followerId,
            @Param("sortBy") String sortBy,
            @Param("order") String order,
            @Param("limit") int limit,
            @Param("offset") int offset
    );

    // Lấy số lượng người theo dõi của user có id là userId
    @Query("SELECT COUNT(f) FROM Follow f WHERE f.following.id = :userId")
    Long countFollowersByUserId(@Param("userId") String userId);

    // Lấy danh sách User đang theo dõi user có id là followingId (tác giả)
    @Query("SELECT f.follower FROM Follow f WHERE f.following.id = :authorId")
    List<User> findFollowersByUserId(@Param("authorId") String authorId);
}
