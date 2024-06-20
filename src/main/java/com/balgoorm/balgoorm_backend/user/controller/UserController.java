package com.balgoorm.balgoorm_backend.user.controller;

import com.balgoorm.balgoorm_backend.user.auth.CustomUserDetails;
import com.balgoorm.balgoorm_backend.user.dto.request.UserLoginRequest;
import com.balgoorm.balgoorm_backend.user.dto.request.UserSignupRequest;
import com.balgoorm.balgoorm_backend.user.dto.request.UserUpdateRequest;
import com.balgoorm.balgoorm_backend.user.model.entity.User;
import com.balgoorm.balgoorm_backend.user.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api")
public class UserController {

    private final UserService userService;

    @Autowired
    public UserController(UserService userService) {
        this.userService = userService;
    }

    // 전체 회원을 조회하는 API
    @GetMapping("/admin/all")
    public ResponseEntity<List<User>> getAllMembers() {
        List<User> users = userService.getAllMembers();
        return ResponseEntity.ok(users);
    }

    // 총 회원 수를 세어주는 API
    @GetMapping("/admin/totalUsers")
    public ResponseEntity<Long> getTotalUsers() {
        long totalUsers = userService.getTotalUsers();
        return ResponseEntity.ok(totalUsers);
    }

    @PostMapping("/signup")
    public ResponseEntity<String> signup(@RequestBody UserSignupRequest request) {
        if (request.getUserId().isEmpty()) {
            return new ResponseEntity<>("아이디가 비어있습니다", HttpStatus.BAD_REQUEST);
        }
        if (request.getPassword().isEmpty()) {
            return new ResponseEntity<>("비밀번호가 비어있습니다", HttpStatus.BAD_REQUEST);
        }
        if (request.getNickname().isEmpty()) {
            return new ResponseEntity<>("닉네임이 비었습니다", HttpStatus.BAD_REQUEST);
        }
        if (request.getEmail().isEmpty()) {
            return new ResponseEntity<>("이메일이 비어있습니다", HttpStatus.BAD_REQUEST);
        }

        userService.signup(request);
        log.info("회원가입 {}", request);
        return new ResponseEntity<>("회원가입 성공!", HttpStatus.CREATED);
    }

    // 회원 정보 수정 (닉네임, 비밀번호)
    @PostMapping("/updateUser/{userid}") // 비밀번호와 닉네임 변경을 따로 만들지 고민 중
    public ResponseEntity<String> updateUser(@PathVariable("userid") String userId, @RequestBody UserUpdateRequest request) {
        userService.updateUser(request, userId);
        return ResponseEntity.ok("회원 정보가 수정되었습니다.");
    }

    // 회원 탈퇴
    @DeleteMapping("/deleteUser/{userid}")
    public ResponseEntity<String> deleteUser(@PathVariable("userid") String userId) {
        userService.deleteUser(userId);
        return ResponseEntity.ok("회원이 탈퇴되셨습니다.");
    }


}
