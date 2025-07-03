package com.balgoorm.balgoorm_backend.user.service;

import com.balgoorm.balgoorm_backend.user.model.dto.request.UserSignupRequest;
import com.balgoorm.balgoorm_backend.user.model.dto.request.UserUpdateRequest;
import com.balgoorm.balgoorm_backend.user.model.dto.response.MyInfoResponseDTO;
import com.balgoorm.balgoorm_backend.user.model.dto.response.UserResponseDTO;
import com.balgoorm.balgoorm_backend.user.model.entity.User;
import com.balgoorm.balgoorm_backend.user.model.entity.UserRole;
import com.balgoorm.balgoorm_backend.user.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Autowired
    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    /** 전체 회원 리스트 (DTO 변환) */
    @Transactional(readOnly = true)
    public List<UserResponseDTO> getAllMembers() {
        return userRepository.findAll().stream()
                .map(UserResponseDTO::from)
                .toList();
    }

    /** 전체 회원 수 */
    @Transactional(readOnly = true)
    public long getTotalUsers() {
        return userRepository.count();
    }

    /** 회원가입 (중복/검증 체크, 엔티티 자동 생성일 적용) */
    @Transactional
    public void signup(UserSignupRequest request) {
        if (userRepository.existsByUserId(request.getUserId())) {
            throw new IllegalArgumentException("유저 아이디가 중복됩니다.");
        }
        if (userRepository.existsByNickname(request.getNickname())) {
            throw new IllegalArgumentException("닉네임이 중복됩니다.");
        }
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("이메일이 중복됩니다.");
        }

        User user = User.builder()
                .userId(request.getUserId())
                .userPassword(passwordEncoder.encode(request.getPassword()))
                .nickname(request.getNickname())
                .email(request.getEmail())
                .role(UserRole.USER)
                .build();
        userRepository.save(user);
    }

    /** 회원 정보 수정 (닉네임/비밀번호) */
    @Transactional
    public void updateUser(UserUpdateRequest request, String userId) {
        User user = userRepository.findByUserId(userId)
                .orElseThrow(() -> new IllegalArgumentException("유저를 찾을 수 없습니다."));

        // 닉네임 변경
        if (request.getNickname() != null && !request.getNickname().isBlank()
                && !request.getNickname().equals(user.getNickname())) {
            if (userRepository.existsByNickname(request.getNickname())) {
                throw new IllegalArgumentException("닉네임이 중복됩니다.");
            }
            user.setNickname(request.getNickname());
        }
        // 비밀번호 변경
        if (request.getPassword() != null && !request.getPassword().isBlank()) {
            user.setUserPassword(passwordEncoder.encode(request.getPassword()));
        }

        userRepository.save(user);
    }

    /** 비밀번호 일치 확인 */
    @Transactional(readOnly = true)
    public boolean checkPassword(String userId, String rawPassword) {
        return userRepository.findByUserId(userId)
                .map(user -> passwordEncoder.matches(rawPassword, user.getUserPassword()))
                .orElse(false);
    }

    /** 회원 삭제 (관리자 보호) */
    @Transactional
    public void deleteUser(String userId) {
        if ("admin".equals(userId)) {
            throw new IllegalArgumentException("관리자 계정은 삭제할 수 없습니다.");
        }
        User user = userRepository.findByUserId(userId)
                .orElseThrow(() -> new IllegalArgumentException("유저를 찾을 수 없습니다."));
        userRepository.delete(user);
    }

    /** 내 정보 조회 (DTO 반환) */
    @Transactional(readOnly = true)
    public MyInfoResponseDTO getUserInfo(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UsernameNotFoundException("해당 아이디의 유저를 찾을 수 없습니다. " + userId));
        return MyInfoResponseDTO.from(user);
    }

}