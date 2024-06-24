package com.balgoorm.balgoorm_backend.quiz.service;

import com.balgoorm.balgoorm_backend.quiz.model.dto.request.RequestSaveQuiz;
import com.balgoorm.balgoorm_backend.quiz.model.dto.response.ResponseQuizDetail;
import com.balgoorm.balgoorm_backend.quiz.model.dto.response.ResponseQuizList;
import com.balgoorm.balgoorm_backend.quiz.model.entity.Quiz;
import com.balgoorm.balgoorm_backend.quiz.model.entity.UserRecQuiz;
import com.balgoorm.balgoorm_backend.quiz.model.enums.QuizSortType;
import com.balgoorm.balgoorm_backend.quiz.repository.*;
import com.balgoorm.balgoorm_backend.user.model.entity.User;
import com.balgoorm.balgoorm_backend.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class QuizServiceImpl implements QuizService{

    private final QuizRepository quizRepository;
    private final CustomQuizRepository customQuizRepository;
    private final UserRecQuizRepository userRecQuizRepository;
    private final UserRepository userRepository;
    private final SolvedQuizRepository solvedQuizRepository;

    @Value("${quiz.list.size}")
    private int pageSize;

    @Override
    public List<ResponseQuizList> getQuizList(QuizSortType sortType, List<Integer> levels, Long userId, int page) {

        HashSet<Long> quizIdSet = solvedQuizRepository.findQuizIdsByUserId(userId);

        List<Quiz> quizList = customQuizRepository.getQuizListSorted(PageRequest.of(page, pageSize), levels, sortType);

        List<ResponseQuizList> responseQuizList = quizList.stream().map(quiz ->
            new ResponseQuizList().builder()
                    .quizId(quiz.getQuizId())
                    .quizTitle(quiz.getQuizTitle())
                    .quiz_level(quiz.getQuizLevel())
                    .quiz_rec_cnt(quiz.getQuizRecCnt())
                    .correct_rate(quiz.getSubmitCnt() == 0 ? 0 : quiz.getCorrectCnt() * 100 / quiz.getSubmitCnt())
                    .isSolved(quizIdSet.contains(quiz.getQuizId()))
                    .build()).collect(Collectors.toList());


        return responseQuizList;
    }

    @Override
    public ResponseQuizDetail getQuizDetail(Long quizId, Long userId) {

        Optional<Quiz> findQuiz = quizRepository.findById(quizId);
        if (!findQuiz.isPresent()) {
            // TODO : throw Exception ( can't find quiz , custom Exception)
            throw new RuntimeException("Quiz not found");
        }

        // TODO : find User ( user repository 연동 후에 작업 )
        Optional<User> findUser = userRepository.findById(userId);
        if(!findQuiz.isPresent()){
            // TODO : throw Exception ( can't find user , custom Exception)
            throw new RuntimeException("User not found");
        }

        User user = findUser.get();

        Optional<UserRecQuiz> userRecQuiz = userRecQuizRepository.findByUserAndQuiz(user, findQuiz.get());

        ResponseQuizDetail quizDetail = ResponseQuizDetail.builder()
                .quizId(quizId)
                .quizTitle(findQuiz.get().getQuizTitle())
                .quizContent(findQuiz.get().getQuizContent())
                .quizPoint(findQuiz.get().getQuizPoint())
                .quizRegDate(findQuiz.get().getQuizRegDate())
                .quizLevel(findQuiz.get().getQuizLevel())
                .quizRecCnt(findQuiz.get().getQuizRecCnt())
                .isRec(userRecQuiz.isPresent()) // 추천 목록에 있다면 추천한 전적이 있다는 것
                .build();

        return quizDetail;
    }

    @Override
    public void saveQuiz(RequestSaveQuiz requestSaveQuiz) {

        Optional<User> findUser = userRepository.findById(requestSaveQuiz.getUserId());

        if (!findUser.isPresent()) {
            // TODO : throw Exception ( can't find user , custom Exception )
            throw new RuntimeException("User not found");
        }

        Quiz saveQuiz = Quiz.builder()
                .user(findUser.get())
                .quizTitle(requestSaveQuiz.getQuizTitle())
                .quizContent(requestSaveQuiz.getQuizContent())
                .quizPoint(requestSaveQuiz.getQuizPoint())
                .quizRegDate(LocalDateTime.now())
                .quizRecCnt(0)
                .quizLevel(requestSaveQuiz.getQuizLevel())
                .quizAnswer(requestSaveQuiz.getQuizAnswer())
                .correctCnt(0)
                .submitCnt(0)
                .build();

        quizRepository.save(saveQuiz);

    }
}
