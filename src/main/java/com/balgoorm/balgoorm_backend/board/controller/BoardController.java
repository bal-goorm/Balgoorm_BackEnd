package com.balgoorm.balgoorm_backend.board.controller;

import com.balgoorm.balgoorm_backend.board.model.dto.request.BoardEditRequest;
import com.balgoorm.balgoorm_backend.board.model.dto.request.BoardImageUploadDTO;
import com.balgoorm.balgoorm_backend.board.model.dto.request.BoardWriteRequestDTO;
import com.balgoorm.balgoorm_backend.board.model.dto.response.BoardResponseDTO;
import com.balgoorm.balgoorm_backend.board.service.BoardService;
import com.balgoorm.balgoorm_backend.user.auth.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/boards")
public class BoardController {

    private final BoardService boardService;

    @Value("${board.list.size}")
    private int pageSize;


    @GetMapping("/{id}")
    public BoardResponseDTO searchBoard(@PathVariable("id") Long boardId) {
        return boardService.searchBoard(boardId);
    }

    @GetMapping
    public List<BoardResponseDTO> searchBoardList(
            @RequestParam("page") int page,
            @RequestParam("direction") Sort.Direction direction,
            @RequestParam("sortBy") String sortBy) {
        return boardService.searchBoardList(page, pageSize, direction.name(), sortBy);
    }


    @PostMapping
    public BoardResponseDTO createBoard(BoardWriteRequestDTO boardWriteRequestDTO,
                                        Authentication authentication) throws IOException {
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        Long boardId = boardService.saveBoard(boardWriteRequestDTO, userDetails.getUsername());
        return boardService.searchBoard(boardId);
    }


    @PutMapping("/{id}")
    public BoardResponseDTO editBoard(@PathVariable("id") Long boardId,
                                      @ModelAttribute BoardEditRequest boardEditRequest,
                                      Authentication authentication) throws IOException {
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        return boardService.editBoard(boardId, boardEditRequest, userDetails.getUsername());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteBoard(@PathVariable("id") Long boardId, Authentication authentication) {
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        boardService.deleteBoard(boardId, userDetails.getUsername());

        return ResponseEntity.ok("게시글이 성공적으로 삭제되었습니다.");
    }


    @PostMapping("/{id}/like")
    public String likeBoard(@PathVariable("id") Long boardId, Authentication authentication) {
        try {
            CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
            Long userId = userDetails.getUserId();
            boardService.likeBoard(boardId, userId);
            return "Liked the board";
        } catch (IllegalArgumentException e) {
            return e.getMessage();
        }
    }

    @PostMapping("/{id}/unlike")
    public String unlikeBoard(@PathVariable("id") Long boardId, Authentication authentication) {
        String username = authentication.getName();
        boardService.unlikeBoard(boardId, Long.valueOf(username));
        return "Unliked the board";
    }
}