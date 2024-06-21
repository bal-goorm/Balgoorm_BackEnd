package com.balgoorm.balgoorm_backend.quiz.controller;

import com.balgoorm.balgoorm_backend.quiz.model.dto.request.RequestSaveQuiz;
import com.balgoorm.balgoorm_backend.quiz.model.dto.response.ResponseQuizDetail;
import com.balgoorm.balgoorm_backend.quiz.model.dto.response.ResponseQuizList;
import com.balgoorm.balgoorm_backend.quiz.model.enums.QuizSortType;
import com.balgoorm.balgoorm_backend.quiz.service.QuizService;
import com.balgoorm.balgoorm_backend.user.auth.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/quiz")
@Slf4j
public class QuizController {

    private final QuizService quizService;

    /**
     * 퀴즈 목록을 가져오는 API
     * @param sortType 정렬할 타입 ( 입력되지 않으면 기본으로 설정 )
     * @param page ( 현재 목록의 페이지 수 ->  1부터 시작)
     * @param levels ( 보고싶은 문제 난이도 선택 / ?levels=1,2,3 <- 이런식으로 요청하면 됩니다 )
     * @return
     */
    @GetMapping("/list/{page}")
    public ResponseEntity getQuizList(@RequestParam(defaultValue = "DEFAULT") QuizSortType sortType ,@PathVariable int page
            , @RequestParam(defaultValue = "") List<Integer> levels, Authentication authentication){

        /**
         * SpringDataJPA 에서 사용한 페이징 시스템의 시작 페이지는 0 부터 들어오는 변수는 1부터 들어오기 떄문에 -1 해서 서비스 메소드로 전달
         */

        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        Long userId = userDetails.getUserId();

        List<ResponseQuizList> quizList = quizService.getQuizList(sortType, levels, userId,  page-1);

        return ResponseEntity.ok(quizList);
    }

    @GetMapping("/detail/{quizId}")
    public ResponseEntity getQuizDetail(@PathVariable Long quizId, Authentication authentication){

        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        Long userId = userDetails.getUserId();

        ResponseQuizDetail quizDetail = quizService.getQuizDetail(quizId, userId);

        return ResponseEntity.ok(quizDetail);
    }




    @PostMapping("/save")
    public ResponseEntity saveQuiz(@RequestBody RequestSaveQuiz requestSaveQuiz, Authentication authentication){

        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        Long userId = userDetails.getUserId();

        requestSaveQuiz.setUserId(userId);

        quizService.saveQuiz(requestSaveQuiz);

        return ResponseEntity.ok("정상적으로 저장되었습니다");
    }



}
