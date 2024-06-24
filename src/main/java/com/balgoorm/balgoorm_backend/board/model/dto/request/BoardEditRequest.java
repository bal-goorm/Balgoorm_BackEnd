package com.balgoorm.balgoorm_backend.board.model.dto.request;

import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Data
public class BoardEditRequest {
    private String boardTitle;
    private String boardContent;
//    private List<MultipartFile> files;
}
