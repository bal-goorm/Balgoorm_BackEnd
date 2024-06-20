package com.balgoorm.balgoorm_backend.user.dto.response;

import com.balgoorm.balgoorm_backend.user.model.entity.User;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class MyInfoResponseDTO {
    private String userId;
    private String nickname;
    private String email;
    private LocalDateTime createDate;


    public MyInfoResponseDTO(User user) {

        this.userId = user.getUserId();
        this.nickname = user.getNickname();
        this.email = user.getEmail();
        this.createDate = user.getCreateDate();

    }
}
