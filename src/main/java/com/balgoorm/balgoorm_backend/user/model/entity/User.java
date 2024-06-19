package com.balgoorm.balgoorm_backend.user.model.entity;

import jakarta.persistence.*;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@Entity
@NoArgsConstructor
@Table(name = "USERS")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String userId;

    @Column(nullable = false)
    private String userPassword;

    @Column(nullable = false)
    private String nickname;

    @Column(nullable = false)
    private String email;

    private LocalDateTime createDate;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private UserRole role;

//    @ManyToOne
//    @JoinColumn(name = "CHAT_ROOM_ID", nullable = false)
//    private ChatRoom chatRoom;

    // All args constructor for Builder pattern
    @Builder
    public User(Long id, String userId, String userPassword, String nickname, String email, LocalDateTime createDate, UserRole role) {
        this.id = id;
        this.userId = userId;
        this.userPassword = userPassword;
        this.nickname = nickname;
        this.email = email;
        this.createDate = createDate;
        this.role = role;
//        this.chatRoom = chatRoom;
    }
}
