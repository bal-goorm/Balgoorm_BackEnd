package com.balgoorm.balgoorm_backend.user.dto.response;

import com.balgoorm.balgoorm_backend.user.model.entity.User;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class UserResponseDTO {
    private Long id;
    private String userId;
//    private String password;
    private String nickname;
    private String email;
    private LocalDateTime createDate;
    private String role;

    public UserResponseDTO(User user) {
        this.id = user.getId();
        this.userId = user.getUserId();
//        this.password = user.getUserPassword();
        this.nickname = user.getNickname();
        this.email = user.getEmail();
        this.createDate = user.getCreateDate();
        this.role = user.getRole().toString();
    }
}
