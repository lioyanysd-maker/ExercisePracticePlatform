package com.lio.exercisepracticesystem.repository;

import com.lio.exercisepracticesystem.entity.ErrorBook;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ErrorBookRepository extends JpaRepository<ErrorBook, Long> {

    Optional<ErrorBook> findByUserIdAndSubjectIdAndQuestionId(Long userId, Long subjectId, Long questionId);

    List<ErrorBook> findByUserIdOrderByLastWrongAtDesc(Long userId);

    List<ErrorBook> findByUserIdAndSubjectIdOrderByLastWrongAtDesc(Long userId, Long subjectId);

    long countByUserId(Long userId);

    long countByUserIdAndSubjectId(Long userId, Long subjectId);

    @Query("SELECT DISTINCT q.type FROM Question q JOIN ErrorBook e ON q.id = e.questionId WHERE e.userId = :userId AND e.subjectId = :subjectId")
    List<com.lio.exercisepracticesystem.entity.QuestionType> findDistinctQuestionTypesByUserIdAndSubjectId(@Param("userId") Long userId, @Param("subjectId") Long subjectId);
}

