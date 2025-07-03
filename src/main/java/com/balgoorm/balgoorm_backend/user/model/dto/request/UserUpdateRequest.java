package com.balgoorm.balgoorm_backend.user.model.dto.request;

import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class UserUpdateRequest {
    @Size(min = 2, max = 15, message = "닉네임은 2~15자 이내로 입력해주세요.")
    private String nickname;

    @Size(min = 8, message = "비밀번호는 8자 이상이어야 합니다.")
    private String password;
}
