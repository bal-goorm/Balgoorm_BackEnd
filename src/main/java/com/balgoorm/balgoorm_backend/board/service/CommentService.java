package com.balgoorm.balgoorm_backend.board.service;

import com.balgoorm.balgoorm_backend.board.model.dto.request.CommentRequestDTO;
import com.balgoorm.balgoorm_backend.board.model.dto.response.CommentResponseDTO;
import com.balgoorm.balgoorm_backend.board.model.entity.Board;
import com.balgoorm.balgoorm_backend.board.model.entity.Comment;
import com.balgoorm.balgoorm_backend.board.model.entity.Likes;
import com.balgoorm.balgoorm_backend.board.repository.BoardRepository;
import com.balgoorm.balgoorm_backend.board.repository.CommentRepository;
import com.balgoorm.balgoorm_backend.board.repository.LikesRepository;
import com.balgoorm.balgoorm_backend.user.model.entity.User;
import com.balgoorm.balgoorm_backend.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class CommentService {

    private final CommentRepository commentRepository;
    private final BoardRepository boardRepository;
    private final UserRepository userRepository;
    private final LikesRepository likesRepository;

    @Transactional
    public CommentResponseDTO writeComment(CommentRequestDTO commentRequestDTO, Long boardId, Long userId) {
        Board board = boardRepository.findById(boardId).orElseThrow(() -> new IllegalArgumentException("Invalid board Id"));
        User user = userRepository.findById(userId).orElseThrow(() -> new IllegalArgumentException("Invalid user Id"));

        Comment comment = Comment.builder()
                .commentContent(commentRequestDTO.getCommentContent())
                .board(board)
                .user(user)
                .commentCreateDate(LocalDateTime.now())
                .build();

        commentRepository.save(comment);
        return new CommentResponseDTO(comment);
    }

    @Transactional
    public CommentResponseDTO updateComment(CommentRequestDTO commentRequestDTO, Long commentId, Long userId) {
        Comment comment = commentRepository.findById(commentId).orElseThrow(() -> new IllegalArgumentException("Invalid comment Id"));
        if (!comment.getUser().getId().equals(userId)) {
            throw new IllegalArgumentException("다른 사용자의 댓글은 수정할 수 없습니다.");
        }
        comment.setCommentContent(commentRequestDTO.getCommentContent());
        commentRepository.save(comment);
        return new CommentResponseDTO(comment);
    }

    @Transactional
    public void deleteComment(Long commentId, Long userId) {
        Comment comment = commentRepository.findById(commentId).orElseThrow(() -> new IllegalArgumentException("Invalid comment Id"));
        if (!comment.getUser().getId().equals(userId)) {
            throw new IllegalArgumentException("다른 사용자의 댓글은 삭제할 수 없습니다.");
        }
        commentRepository.delete(comment);
    }

    @Transactional
    public void likeComment(Long commentId, Long userId) {
        Comment comment = commentRepository.findById(commentId).orElseThrow(() -> new IllegalArgumentException("Invalid comment Id"));
        User user = userRepository.findById(userId).orElseThrow(() -> new UsernameNotFoundException("User not found with id: " + userId));

        if (!likesRepository.existsByUserIdAndCommentCommentId(userId, commentId)) {
            Likes like = new Likes(user, comment);
            likesRepository.save(like);
            comment.incrementLikes(); // 좋아요 수 증가
            commentRepository.save(comment);
        } else {
            throw new IllegalArgumentException("User already liked this comment");
        }
    }

    @Transactional
    public void unlikeComment(Long commentId, Long userId) {
        Likes like = likesRepository.findByUserIdAndCommentCommentId(userId, commentId)
                .orElseThrow(() -> new IllegalArgumentException("Like not found for user and comment"));
        likesRepository.delete(like);
        Comment comment = like.getComment();
        comment.decrementLikes(); // 좋아요 수 감소
        commentRepository.save(comment);
    }

    @Transactional(readOnly = true)
    public List<CommentResponseDTO> getCommentsByBoardId(Long boardId) {
        return commentRepository.findByBoardBoardId(boardId).stream()
                .map(CommentResponseDTO::new)
                .collect(Collectors.toList());
    }
}
