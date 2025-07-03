package com.balgoorm.balgoorm_backend.user.model.dto.response;

import com.balgoorm.balgoorm_backend.user.model.entity.User;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
public class UserResponseDTO {
    private Long id;
    private String userId;
    private String nickname;
    private String email;
    private LocalDateTime createDate;
    private String role;

    // 정적 팩토리 메서드로 변경
    public static UserResponseDTO from(User user) {
        return new UserResponseDTO(
                user.getId(),
                user.getUserId(),
                user.getNickname(),
                user.getEmail(),
                user.getCreateDate(),
                user.getRole().name()
        );
    }
}
