package com.balgoorm.balgoorm_backend.chat.model.response;

import com.balgoorm.balgoorm_backend.chat.model.entity.Chat;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ChatResponse {
    private String senderName;
    private String chatBody;

    public static ChatResponse changeResponse (Chat chat) {
        return new ChatResponse(
                chat.getSenderName(),
                chat.getChatBody()
        );
    }
}
