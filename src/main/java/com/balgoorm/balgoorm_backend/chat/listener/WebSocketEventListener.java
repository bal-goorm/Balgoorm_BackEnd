package com.balgoorm.balgoorm_backend.chat.listener;

import com.balgoorm.balgoorm_backend.chat.service.ChatService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionConnectedEvent;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

import java.util.HashSet;
import java.util.Set;

@Component
@RequiredArgsConstructor
public class WebSocketEventListener {
    private final SimpMessagingTemplate simpMessagingTemplate;
    private final ChatService chatService;

    //Todo:: Set 중복 허용 안됨 테스트문제 발생 시 확인 필요
    private Set<String> activeUsers = new HashSet<>();

    @EventListener
    public void handleWebSocketConnectListener(SessionConnectedEvent event) {
        // 사용자 추가
        String username = chatService.getCurrentUser().getNickname(); // 실제 사용자명 로직 필요
        activeUsers.add(username);
        updateActiveUsers();
    }

    @EventListener
    public void handleWebSocketDisconnectListener(SessionDisconnectEvent event) {
        // 사용자 제거
        String username = chatService.getCurrentUser().getNickname(); // 실제 사용자명 로직 필요
        activeUsers.remove(username);
        updateActiveUsers();
    }

    // "/sub/active-users" 구독시 소켓 연결, 해체시 현재 채팅방 참여 인원을 String 형태로 발송
    private void updateActiveUsers() {
        simpMessagingTemplate.convertAndSend("/sub/active-users", activeUsers.size());
    }
}
