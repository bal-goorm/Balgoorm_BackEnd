package com.balgoorm.balgoorm_backend.user.controller;

import com.balgoorm.balgoorm_backend.user.auth.CustomUserDetails;
import com.balgoorm.balgoorm_backend.user.model.dto.request.UserSignupRequest;
import com.balgoorm.balgoorm_backend.user.model.dto.request.UserUpdateRequest;
import com.balgoorm.balgoorm_backend.user.model.dto.response.MyInfoResponseDTO;
import com.balgoorm.balgoorm_backend.user.model.dto.response.UserResponseDTO;
import com.balgoorm.balgoorm_backend.user.model.entity.User;
import com.balgoorm.balgoorm_backend.user.service.UserService;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "http://localhost:3000")
public class UserController {

    private final UserService userService;

    @Autowired
    public UserController(UserService userService) {
        this.userService = userService;
    }

    // [ADMIN] 전체 회원 조회 (Entity → DTO 변환)
    @GetMapping("/admin/all")
    public ResponseEntity<List<UserResponseDTO>> getAllMembers() {
        List<UserResponseDTO> users = userService.getAllMembers();
        return ResponseEntity.ok(users);
    }

    // [ADMIN] 총 회원 수 조회
    @GetMapping("/admin/totalUsers")
    public ResponseEntity<Long> getTotalUsers() {
        return ResponseEntity.ok(userService.getTotalUsers());
    }


    // 회원가입 (@Valid 로 입력 검증하여 불필요한 if문 제거)
    @PostMapping("/signup")
    public ResponseEntity<String> signup(@Valid @RequestBody UserSignupRequest request, BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            String msg = bindingResult.getFieldError().getDefaultMessage();
            return ResponseEntity.badRequest().body(msg);
        }
        userService.signup(request);
        log.info("회원가입 {}", request);
        return ResponseEntity.status(HttpStatus.CREATED).body("회원가입 성공!");
    }


    // 회원 정보 수정 (PUT 이 더 적합하다고 판단)
    @PutMapping("/updateUser/{userId}")
    public ResponseEntity<String> updateUser(
            @PathVariable String userId,
            @Valid @RequestBody UserUpdateRequest request,
            BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            String msg = bindingResult.getFieldError().getDefaultMessage();
            return ResponseEntity.badRequest().body(msg);
        }
        userService.updateUser(request, userId);
        return ResponseEntity.ok("회원 정보가 수정되었습니다.");
    }

    // 회원 탈퇴
    @DeleteMapping("/deleteUser/{userId}")
    public ResponseEntity<String> deleteUser(@PathVariable String userId) {
        userService.deleteUser(userId);
        return ResponseEntity.ok("회원이 탈퇴되셨습니다.");
    }

    // 로그인한 유저의 정보 조회
    @GetMapping("/myinfo")
    public ResponseEntity<MyInfoResponseDTO> myinfo(Authentication authentication) {
        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        MyInfoResponseDTO myInfoResponseDTO = userService.getUserInfo(userDetails.getUserId());
        return ResponseEntity.ok(myInfoResponseDTO);
    }
}
