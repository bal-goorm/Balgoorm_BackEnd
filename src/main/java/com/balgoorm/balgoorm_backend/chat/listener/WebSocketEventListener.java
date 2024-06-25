package com.balgoorm.balgoorm_backend.chat.listener;

import com.balgoorm.balgoorm_backend.chat.service.ChatService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.simp.annotation.SubscribeMapping;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Controller;
import org.springframework.web.socket.messaging.SessionConnectedEvent;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

import java.util.HashSet;
import java.util.Set;

@Component
@RequiredArgsConstructor
@Slf4j
public class WebSocketEventListener {
    private final SimpMessagingTemplate simpMessagingTemplate;
    private final ChatService chatService;

    //Todo:: Set 중복 허용 안됨 테스트문제 발생 시 확인 필요
    public static Set<String> activeUsers = new HashSet<>();

    @EventListener
    public void handleWebSocketConnectListener(SessionConnectedEvent event) {

        SimpMessageHeaderAccessor accessor = SimpMessageHeaderAccessor.wrap(event.getMessage());
        String sessionId = accessor.getSessionId();
        // 사용자 추가
        activeUsers.add(sessionId);
        log.info("웹소켓 연결 성공: {}", activeUsers.size());
        updateActiveUsers();
    }

    @EventListener
    public void handleWebSocketDisconnectListener(SessionDisconnectEvent event) {
        // 사용자 제거
        SimpMessageHeaderAccessor accessor = SimpMessageHeaderAccessor.wrap(event.getMessage());
        String sessionId = accessor.getSessionId();
        activeUsers.remove(sessionId);
        log.info("웹소켓 연결 해체: {}", activeUsers.size());
        updateActiveUsers();
    }

    // "/sub/active-users" 구독시 소켓 연결, 해체시 현재 채팅방 참여 인원을 String 형태로 발송
    public void updateActiveUsers() {
        simpMessagingTemplate.convertAndSend("/api/sub/active-users", activeUsers.size());
    }
}
