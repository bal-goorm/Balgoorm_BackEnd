package com.balgoorm.balgoorm_backend.chat.service;

import com.balgoorm.balgoorm_backend.chat.model.entity.Chat;
import com.balgoorm.balgoorm_backend.chat.model.request.ChatRequest;
import com.balgoorm.balgoorm_backend.chat.model.response.ChatResponse;
import com.balgoorm.balgoorm_backend.chat.repository.ChatRepository;
import com.balgoorm.balgoorm_backend.user.auth.CustomUserDetails;
import com.balgoorm.balgoorm_backend.user.model.entity.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ChatService {

    private final ChatRepository chatRepository;

    public Chat enterChat(ChatRequest chatRequest) {
        Chat chat = new Chat();
        chat.setSenderName(chatRequest.getSenderName());
        chat.setChatBody(chatRequest.getChatBody());
        chat.setChatTime(LocalDateTime.now());

        Chat saved = chatRepository.save(chat);
        log.info("saved: {}", saved.getChatBody());

        return saved;
    }
    public ChatResponse enterChat2(ChatRequest chatRequest) {
        Chat chat = new Chat();
        chat.setSenderName(chatRequest.getSenderName());
        chat.setChatBody(chatRequest.getChatBody());
        chat.setChatTime(LocalDateTime.now());

        Chat saved = chatRepository.save(chat);
        log.info("saved: {}", saved.getChatBody());

        return ChatResponse.changeResponse(saved);
    }

    public List<ChatResponse> getHistory() {
        PageRequest pageRequest = PageRequest.of(0, 10);
        return chatRepository.findLatelyChat(pageRequest)
                .stream()
                .map(ChatResponse::changeResponse)
                .collect(Collectors.toList());
    }

    public User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication(); //security 에서 유저정보 가져오기
        if (authentication != null && authentication.getPrincipal() instanceof CustomUserDetails) {
            CustomUserDetails customUserDetails = (CustomUserDetails) authentication.getPrincipal();
            return customUserDetails.getUser();
        }
        return null;
    }

}
