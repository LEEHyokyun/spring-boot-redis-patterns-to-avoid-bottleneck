package com.redis.bottleneck.model.response;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.redis.bottleneck.model.entity.User;
import lombok.Getter;
import lombok.ToString;

import java.time.LocalDateTime;

@Getter
@ToString
public class UserResponse {
    @JsonIgnore
    private Long userId;
    private String loginId;
    private String email;
    @JsonIgnore
    private String password;
    private String name;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static UserResponse from(User user) {
        UserResponse userResponse = new UserResponse();
        userResponse.userId = user.getUserId();
        userResponse.loginId = user.getLoginId();
        userResponse.email = user.getEmail();
        userResponse.password = user.getPassword();
        userResponse.name = user.getName();
        userResponse.createdAt = user.getCreatedAt();
        userResponse.updatedAt = user.getUpdatedAt();

        return userResponse;
    }
}
