package com.balgoorm.balgoorm_backend.board.service;

import com.balgoorm.balgoorm_backend.board.model.dto.request.BoardImageUploadDTO;
import com.balgoorm.balgoorm_backend.board.model.dto.request.BoardWriteRequestDTO;
import com.balgoorm.balgoorm_backend.board.model.dto.request.BoardEditRequest;
import com.balgoorm.balgoorm_backend.board.model.dto.response.BoardResponseDTO;
import com.balgoorm.balgoorm_backend.board.model.entity.Board;
import com.balgoorm.balgoorm_backend.board.model.entity.BoardImage;
import com.balgoorm.balgoorm_backend.board.model.entity.Likes;
import com.balgoorm.balgoorm_backend.board.model.entity.View;
import com.balgoorm.balgoorm_backend.board.repository.BoardRepository;
import com.balgoorm.balgoorm_backend.board.repository.BoardImageRepository;
import com.balgoorm.balgoorm_backend.board.repository.LikesRepository;
import com.balgoorm.balgoorm_backend.board.repository.ViewRepository;
import com.balgoorm.balgoorm_backend.user.model.entity.User;
import com.balgoorm.balgoorm_backend.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class BoardService {

    private final BoardRepository boardRepository;
    private final BoardImageRepository boardImageRepository;
    private final UserRepository userRepository;
    private final LikesRepository likesRepository;
    private final ViewRepository viewRepository;

    @Value("${file.boardImagePath}")
    private String uploadFolder;



    @Transactional(readOnly = true)
    public Board getBoardById(Long boardId) {
        return boardRepository.findById(boardId).orElse(null);
    }

    @Transactional(readOnly = true)
    public BoardResponseDTO searchBoard(Long boardId) {
        Board board = boardRepository.findById(boardId).orElseThrow(() -> new IllegalArgumentException("Board not found with id: " + boardId));
        return new BoardResponseDTO(board);
    }

    @Transactional(readOnly = true)
    public List<BoardResponseDTO> searchBoardList(int page, int pageSize, String direction, String sortBy) {
        Pageable pageable;
        if ("likes".equals(sortBy)) {
            pageable = PageRequest.of(page, pageSize, Sort.by(Sort.Direction.fromString(direction), "likesCount"));
        } else if ("views".equals(sortBy)) {
            pageable = PageRequest.of(page, pageSize, Sort.by(Sort.Direction.fromString(direction), "views"));
        } else {
            pageable = PageRequest.of(page, pageSize, Sort.by(Sort.Direction.fromString(direction), "boardCreateDate"));
        }
        return boardRepository.findAll(pageable)
                .stream()
                .map(BoardResponseDTO::new)
                .collect(Collectors.toList());
    }


    @Transactional
    public Long saveBoard(BoardWriteRequestDTO boardWriteRequestDTO , String userId) {
        User user = userRepository.findByUserId(userId)
                .orElseThrow(() -> new IllegalArgumentException("유저 ID가 존재하지 않습니다."));

        Board board = Board.builder()
                .boardTitle(boardWriteRequestDTO.getBoardTitle())
                .boardContent(boardWriteRequestDTO.getBoardContent())
                .boardCreateDate(LocalDateTime.now())
                .user(user)
                .build();

        boardRepository.save(board);

//        if (boardImageUploadDTO.getFiles() != null && !boardImageUploadDTO.getFiles().isEmpty()) {
//            saveBoardImages(boardImageUploadDTO.getFiles(), board);
//        }

        return board.getBoardId();
    }

    private void saveBoardImages(List<MultipartFile> files, Board board) {
        if (board.getBoardImages() == null) {
            board.setBoardImages(new ArrayList<>());
        }
        for (MultipartFile file : files) {
            UUID uuid = UUID.randomUUID();
            String imageFileName = uuid + "_" + file.getOriginalFilename();

            File destinationFile = new File(uploadFolder + File.separator + imageFileName);

            log.info("파일 업로드 경로: {}", destinationFile.getAbsolutePath());

            try {
                file.transferTo(destinationFile);
            } catch (IOException e) {
                log.error("파일 업로드 중 오류 발생: {}", e.getMessage());
                throw new RuntimeException("파일 업로드 중 오류가 발생했습니다.", e);
            }

            BoardImage image = BoardImage.builder()
                    .url("/uploads/" + imageFileName)
                    .board(board)
                    .build();

            board.getBoardImages().add(image);
            boardImageRepository.save(image);
        }
    }

    @Transactional
    public BoardResponseDTO editBoard(Long boardId, BoardEditRequest boardEditRequest, String userId) throws IOException {
        Board board = boardRepository.findById(boardId)
                .orElseThrow(() -> new IllegalArgumentException("게시글이 존재하지 않습니다."));
        if (!board.getUser().getUserId().equals(userId)) {
            throw new IllegalArgumentException("다른 사용자의 게시글은 수정할 수 없습니다.");
        }
        board.setBoardTitle(boardEditRequest.getBoardTitle());
        board.setBoardContent(boardEditRequest.getBoardContent());


        boardRepository.save(board);
        return new BoardResponseDTO(board);
    }


    @Transactional
    public BoardResponseDTO deleteBoard(Long boardId, String userId) {
        Board board = boardRepository.findById(boardId)
                .orElseThrow(() -> new IllegalArgumentException("게시글이 존재하지 않습니다."));
        if (!board.getUser().getUserId().equals(userId)) {
            throw new IllegalArgumentException("다른 사용자의 게시글은 삭제할 수 없습니다.");
        }
        boardRepository.deleteById(boardId);
        return new BoardResponseDTO(board);
    }

    @Transactional
    public void likeBoard(Long boardId, Long userId) {
        Board board = boardRepository.findById(boardId).orElseThrow(() -> new IllegalArgumentException("Board not found with id: " + boardId));
        User user = userRepository.findById(userId).orElseThrow(() -> new UsernameNotFoundException("User not found with id: " + userId));

        if (!likesRepository.existsByUserIdAndBoardBoardId(userId, boardId)) {
            Likes like = new Likes(user, board);
            likesRepository.save(like);
            board.incrementLikes();
            boardRepository.save(board);
        } else {
            throw new IllegalArgumentException("User already liked this board");
        }
    }

    @Transactional
    public void unlikeBoard(Long boardId, Long userId) {
        Likes like = likesRepository.findByUserIdAndBoardBoardId(userId, boardId)
                .orElseThrow(() -> new IllegalArgumentException("Like not found for user and board"));
        likesRepository.delete(like);
        Board board = like.getBoard();
        board.decrementLikes();
        boardRepository.save(board);
    }

    @Transactional
    public void viewBoard(Long boardId, Long userId) {
        Board board = boardRepository.findById(boardId).orElseThrow(() -> new IllegalArgumentException("Board not found with id: " + boardId));
        User user = userRepository.findById(userId).orElseThrow(() -> new UsernameNotFoundException("User not found with id: " + userId));

        if (!viewRepository.existsByUserIdAndBoardBoardId(userId, boardId)) {
            View view = new View(user, board);
            viewRepository.save(view);
            board.incrementViews();
            boardRepository.save(board);
        }
    }
}
