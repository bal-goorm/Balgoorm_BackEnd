package com.balgoorm.balgoorm_backend.user.service;

import com.balgoorm.balgoorm_backend.user.dto.request.UserLoginRequest;
import com.balgoorm.balgoorm_backend.user.dto.request.UserSignupRequest;
import com.balgoorm.balgoorm_backend.user.dto.request.UserUpdateRequest;
import com.balgoorm.balgoorm_backend.user.dto.response.MyInfoResponseDTO;
import com.balgoorm.balgoorm_backend.user.dto.response.UserResponseDTO;
import com.balgoorm.balgoorm_backend.user.model.entity.User;
import com.balgoorm.balgoorm_backend.user.model.entity.UserRole;
import com.balgoorm.balgoorm_backend.user.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Autowired
    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional(readOnly = true)
    public List<User> getAllMembers() {
        return userRepository.findAll();
    }

    @Transactional(readOnly = true)
    public long getTotalUsers() {
        return userRepository.count();
    }



    @Transactional
    public void signup(UserSignupRequest request) {
        if (request.getUserId().isEmpty()) {
            throw new IllegalArgumentException("유저 아이디가 비어있습니다.");
        } else if (userRepository.existsByUserId(request.getUserId())) {
            throw new IllegalArgumentException("유저 아이디가 중복됩니다.");
        }

        if (request.getPassword().isEmpty()) {
            throw new IllegalArgumentException("비밀번호가 비어있습니다.");
        }
        if (request.getNickname().isEmpty()) {
            throw new IllegalArgumentException("닉네임이 비어있습니다.");
        } else if (userRepository.existsByNickname(request.getNickname())) {
            throw new IllegalArgumentException("닉네임이 중복됩니다.");
        }

        if (request.getEmail().isEmpty()) {
            throw new IllegalArgumentException("이메일이 비어있습니다.");
        } else if (userRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("이메일이 중복됩니다.");
        }

        User user = User.builder()
                .userId(request.getUserId())
                .userPassword(passwordEncoder.encode(request.getPassword()))
                .nickname(request.getNickname())
                .email(request.getEmail())
                .createDate(LocalDateTime.now())
                .role(UserRole.USER) // 기본 역할을 USER로 설정
                .build();
        userRepository.save(user);
    }


    @Transactional
    public void updateUser(UserUpdateRequest request, String userId) {
        Optional<User> optionalUser = userRepository.findByUserId(userId);
        if (optionalUser.isPresent()) {
            User user = optionalUser.get();
            boolean updated = false;
            if (!request.getNickname().isEmpty() && !request.getNickname().equals(user.getNickname())) {
                if (userRepository.existsByNickname(request.getNickname())) {
                    throw new IllegalArgumentException("닉네임이 중복됩니다.");
                }
                user.setNickname(request.getNickname());
                updated = true;
            }
            if (!request.getPassword().isEmpty()) {
                user.setUserPassword(passwordEncoder.encode(request.getPassword()));
                updated = true;
            }
            if (updated) {
                userRepository.save(user);
            }
        } else {
            throw new IllegalArgumentException("유저를 찾을 수 없습니다.");
        }
    }

    @Transactional(readOnly = true)
    public boolean checkPassword(String userId, String rawPassword) {
        Optional<User> optionalUser = userRepository.findByUserId(userId);
        if (optionalUser.isPresent()) {
            User user = optionalUser.get();
            return passwordEncoder.matches(rawPassword, user.getUserPassword());
        }
        return false;
    }

    @Transactional
    public void deleteUser(String userId) {
        User user = userRepository.findByUserId(userId)
                .orElseThrow(() -> new IllegalArgumentException("유저를 찾을 수 없습니다."));
        userRepository.delete(user);
    }

    @Transactional(readOnly = true)
    public MyInfoResponseDTO getUserInfo(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UsernameNotFoundException("해당 아이디의 유저를 찾을 수 없습니다. " + userId));
        return new MyInfoResponseDTO(user);
    }

}
