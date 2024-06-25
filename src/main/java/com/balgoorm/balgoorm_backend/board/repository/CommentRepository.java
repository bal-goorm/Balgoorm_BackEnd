package com.balgoorm.balgoorm_backend.board.repository;

import com.balgoorm.balgoorm_backend.board.model.entity.Board;
import com.balgoorm.balgoorm_backend.board.model.entity.Comment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CommentRepository extends JpaRepository<Comment, Long> {
    List<Comment> findByBoardBoardId(Long boardId);
}
