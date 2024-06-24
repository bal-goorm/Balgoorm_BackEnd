package com.balgoorm.balgoorm_backend.quiz.model.dto.response;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ResponseQuizList {

    /**
     *  quizId : 퀴즈 식별 번호
     *  quizTitle : 퀴즈 제목
     *  quiz_level : 퀴즈 난이도
     *  quiz_rec_cnt : 퀴즈 추천 수
     *  correct_rate : 퀴즈 정답률
     */
    Long quizId;
    String quizTitle;
    int quiz_level;
    int quiz_rec_cnt;
    int correct_rate;
    boolean isSolved;

}
