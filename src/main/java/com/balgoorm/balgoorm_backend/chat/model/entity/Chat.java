package com.balgoorm.balgoorm_backend.chat.model.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.ChronoField;

@Entity
@Data
public class Chat {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long chatId;
    private String senderName;
    private String chatBody;
    private LocalTime chatTime;

    //test
    @PrePersist
    @PreUpdate
    public void validateTime() {
        if (chatTime != null && chatTime.getNano() < 0) {
            chatTime = chatTime.with(ChronoField.NANO_OF_SECOND, 0);
        }
    }

//    @ManyToOne
//    @JoinColumn(name = "CHATROOM_ID", nullable = false)
//    private ChatRoom chatroom;
}
