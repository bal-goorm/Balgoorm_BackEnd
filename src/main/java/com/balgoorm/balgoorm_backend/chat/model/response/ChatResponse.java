package com.balgoorm.balgoorm_backend.chat.model.response;

import com.balgoorm.balgoorm_backend.chat.model.entity.Chat;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class ChatResponse {
    private String senderName;
    private String chatBody;
    private LocalDateTime chatTime;

    public static ChatResponse changeResponse (Chat chat) {
        return new ChatResponse(
                chat.getSenderName(),
                chat.getChatBody(),
                chat.getChatTime()
        );
    }
}
