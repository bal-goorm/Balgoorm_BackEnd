package com.balgoorm.balgoorm_backend.board.controller;

import com.balgoorm.balgoorm_backend.board.model.dto.request.CommentRequestDTO;
import com.balgoorm.balgoorm_backend.board.model.dto.response.CommentResponseDTO;
import com.balgoorm.balgoorm_backend.board.service.CommentService;
import com.balgoorm.balgoorm_backend.user.auth.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/board")
public class CommentController {

    private final CommentService commentService;

    @PostMapping("/{id}/comment")
    public CommentResponseDTO writeComment(@PathVariable Long id, @RequestBody CommentRequestDTO commentRequestDTO, Authentication authentication) {
        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        Long userId = userDetails.getUserId();
        return commentService.writeComment(commentRequestDTO, id, userId);
    }

    @PutMapping("/{id}/comment/{commentId}")
    public CommentResponseDTO updateComment(@PathVariable Long id, @PathVariable Long commentId,
                                            @RequestBody CommentRequestDTO commentRequestDTO,
                                            Authentication authentication) {
        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        Long userId = userDetails.getUserId();
        return commentService.updateComment(commentRequestDTO, commentId, userId);
    }

    @DeleteMapping("/{id}/comment/{commentId}")
    public String deleteComment(@PathVariable Long id, @PathVariable Long commentId, Authentication authentication) {
        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        Long userId = userDetails.getUserId();
        commentService.deleteComment(commentId, userId);
        return "Comment deleted successfully.";
    }

    @PostMapping("/{id}/comment/{commentId}/like")
    public String likeComment(@PathVariable Long id, @PathVariable Long commentId, Authentication authentication) {
        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        Long userId = userDetails.getUserId();
        commentService.likeComment(commentId, userId);
        return "Liked the comment";
    }

    @PostMapping("/{id}/comment/{commentId}/unlike")
    public String unlikeComment(@PathVariable Long id, @PathVariable Long commentId, Authentication authentication) {
        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        Long userId = userDetails.getUserId();
        commentService.unlikeComment(commentId, userId);
        return "Unliked the comment";
    }
}