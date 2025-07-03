package com.balgoorm.balgoorm_backend.user.model.dto.request;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserLoginRequest {

    private String userId;
    private String password;
}