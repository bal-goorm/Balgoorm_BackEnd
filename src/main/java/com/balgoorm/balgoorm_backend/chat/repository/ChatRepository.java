package com.balgoorm.balgoorm_backend.chat.repository;

import com.balgoorm.balgoorm_backend.chat.model.entity.Chat;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ChatRepository extends JpaRepository<Chat, Long> {

    //저장된 시간 기준 가장 최근 100개의 채팅 내역을 가져 옴
    @Query("SELECT m FROM Chat m ORDER BY m.chatId DESC")
    List<Chat> findLatelyChat(Pageable pageable);
}
