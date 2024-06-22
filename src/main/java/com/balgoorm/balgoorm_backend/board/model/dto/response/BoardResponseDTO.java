package com.balgoorm.balgoorm_backend.board.model.dto.response;

import com.balgoorm.balgoorm_backend.board.model.entity.Board;
import com.balgoorm.balgoorm_backend.board.model.entity.BoardImage;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Getter
public class BoardResponseDTO {
    private Long boardId;
    private String boardTitle;
    private String boardContent;
    private LocalDateTime boardCreateDate;
//    private List<String> imageUrls;
    private List<CommentResponseDTO> comments; // 댓글 리스트 추가
    private int likesCount;
    private int views;
    private String userId;
    private String Nickname;// 작성자 ID 추가

    @Builder
    public BoardResponseDTO(Board board) {
        this.boardId = board.getBoardId();
        this.boardTitle = board.getBoardTitle();
        this.boardContent = board.getBoardContent();
        this.boardCreateDate = board.getBoardCreateDate();
//        this.imageUrls = board.getBoardImages().stream()
//                .map(BoardImage::getUrl)
//                .collect(Collectors.toList());
        this.comments = board.getComments().stream()
                .map(CommentResponseDTO::new)
                .collect(Collectors.toList());
        this.likesCount = board.getLikesCount();
        this.views = board.getViewCount();
        this.userId = board.getUser().getUserId();
        this.Nickname = board.getUser().getNickname();// 작성자 ID 설정
    }
}
