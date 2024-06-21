package com.balgoorm.balgoorm_backend.quiz.repository;

import com.balgoorm.balgoorm_backend.quiz.model.entity.SolvedQuiz;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

public interface SolvedQuizRepository extends JpaRepository<SolvedQuiz, Long> {

    Optional<SolvedQuiz> findByQuizIdAndUserId(Long quizId, Long userId);

    @Query("SELECT sq.quizId FROM SolvedQuiz sq WHERE sq.userId = :userId")
    HashSet<Long> findQuizIdsByUserId(@Param("userId") Long userId);

}
