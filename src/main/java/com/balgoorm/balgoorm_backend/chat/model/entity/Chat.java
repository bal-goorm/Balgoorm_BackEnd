package com.balgoorm.balgoorm_backend.chat.model.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Entity
@Data
public class Chat {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long chatId;
    private String senderName;
    private String chatBody;
    private LocalDateTime chatTime;

//    @ManyToOne
//    @JoinColumn(name = "CHATROOM_ID", nullable = false)
//    private ChatRoom chatroom;
}
