package com.redis.bottleneck.model.request;

import lombok.Getter;
import lombok.ToString;

@Getter
@ToString
public class UserCreateRequest {
    private String loginId;
    private String email;
    private String password;
    private String name;
}
