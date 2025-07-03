package com.balgoorm.balgoorm_backend.user.model.dto.response;

import com.balgoorm.balgoorm_backend.user.model.entity.User;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
public class MyInfoResponseDTO {
    private String userId;
    private String nickname;
    private String email;
    private LocalDateTime createDate;

    // 정적 팩토리 메서드로 변경
    public static MyInfoResponseDTO from(User user) {
        return new MyInfoResponseDTO(
                user.getUserId(),
                user.getNickname(),
                user.getEmail(),
                user.getCreateDate()
        );
    }
}
