package com.balgoorm.balgoorm_backend.user.model.dto.request;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
public class UserRequest {

    private String userId;
    private String password;
    private String nickName;

    private LocalDateTime createDate = LocalDateTime.now();
}
