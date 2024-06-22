package com.balgoorm.balgoorm_backend.quiz.service;

import com.balgoorm.balgoorm_backend.quiz.model.dto.request.RequestSaveQuiz;
import com.balgoorm.balgoorm_backend.quiz.model.dto.response.ResponseQuizDetail;
import com.balgoorm.balgoorm_backend.quiz.model.dto.response.ResponseQuizList;
import com.balgoorm.balgoorm_backend.quiz.model.enums.QuizSortType;
import org.springframework.data.domain.Page;

import java.util.List;


public interface QuizService {

    public List<ResponseQuizList> getQuizList(QuizSortType sortType, List<Integer> levels, Long userId,  int page);

    ResponseQuizDetail getQuizDetail(Long quizId, Long userId);

    void saveQuiz(RequestSaveQuiz requestSaveQuiz);
}
