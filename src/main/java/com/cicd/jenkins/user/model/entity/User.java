package com.cicd.jenkins.user.model.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@Entity
@Table(name = "users")
@NoArgsConstructor
@AllArgsConstructor
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long userId;

    private String loginId;
    private String email;
    private String password;
    private String name;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static User create(String email, String loginId, String password, String name){

        User  user = new User();

        user.loginId = loginId;
        user.email = email;
        user.password = password;
        user.name = name;
        user.createdAt = LocalDateTime.now();
        user.updatedAt = user.createdAt;

        return user;
    }
}
