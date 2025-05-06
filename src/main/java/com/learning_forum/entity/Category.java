package com.learning_forum.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import java.util.Set;

@Setter
@Getter
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Table(name = "categories")
public class Category {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    String id;

    @Column(nullable = false, unique = true)
    private String name;

    // Danh mục cha
    @ManyToOne
    @JoinColumn(name = "parent_id")
    Category parentCategory;

    // Danh sách danh mục con
    @OneToMany(mappedBy = "parentCategory", cascade = CascadeType.ALL, orphanRemoval = true)
    Set<Category> subCategories;

    // Danh sách bài viết thuộc danh mục
    @OneToMany(mappedBy = "category", cascade = CascadeType.ALL)
    Set<Post> posts;
}