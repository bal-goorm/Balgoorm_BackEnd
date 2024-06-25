package com.balgoorm.balgoorm_backend.chat.controller;

import com.balgoorm.balgoorm_backend.chat.listener.WebSocketEventListener;
import com.balgoorm.balgoorm_backend.chat.model.entity.Chat;
import com.balgoorm.balgoorm_backend.chat.model.request.ChatRequest;
import com.balgoorm.balgoorm_backend.chat.model.response.ChatResponse;
import com.balgoorm.balgoorm_backend.chat.service.ChatService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.simp.annotation.SubscribeMapping;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Slf4j
@RestController
@RequiredArgsConstructor
public class ChatController {

    private final ChatService chatService;
//    private final SimpMessagingTemplate simpMessagingTemplate;

    @MessageMapping("/join")
    @SendTo("/api/sub/join")
    public String joinChatRoom(ChatRequest chatRequest) {
        log.info("join: {}", chatRequest.getChatBody());
        return chatRequest.getSenderName() + ":" + chatRequest.getSenderName() + "님이 입장하셨습니다.";
    }

    @MessageMapping("/disconnect")
    @SendTo("/api/sub/disconnect")
    public String disConnectChatRoom(ChatRequest chatRequest) {
        log.info("disconnect: {}", chatRequest.getChatBody());
        return chatRequest.getSenderName() + ":" + chatRequest.getSenderName() + "님이 퇴장하셨습니다.";
    }


    @MessageMapping("/chat")
    @SendTo("/api/sub/chat")
    public String enterChat(ChatRequest chatRequest) {
        Chat savedChat = chatService.enterChat(chatRequest);
        log.info("Message: {}", chatRequest.getChatBody());
        log.info("Message: {}", savedChat.getSenderName());
        return savedChat.getSenderName() + ":" + savedChat.getChatBody()
                + ":" + savedChat.getChatTime();
    }

//    @SubscribeMapping("/sub/active-users")
//    @SendTo("/api/sub/active-users")
//    public String handleSubscription(SimpMessageHeaderAccessor accessor) {
//        String sessionId = accessor.getSessionId();
//        log.info("구독 성공: {}", sessionId);
//        return WebSocketEventListener.activeUsers.toString();
//    }

//    @GetMapping("/active-users")
//    public ResponseEntity<String> getActiveUsers() {
//        return WebSocketEventListener.activeUsers.toString();
//    }

    @GetMapping("/api/history")
    public ResponseEntity<List<ChatResponse>> getHistory() {
        List<ChatResponse> chatHistory = chatService.getHistory();
        return new ResponseEntity<>(chatHistory, HttpStatus.OK);
    }


/*        @MessageMapping("/join")
    public void joinChatRoom(ChatRequest chatRequest) {
        simpMessagingTemplate.convertAndSend("/sub/chat", chatRequest.getName() + "님이 입장하셨습니다.");
        log.info("Message {}", chatRequest.getMessage());
    }*/
}
