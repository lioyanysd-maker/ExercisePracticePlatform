package com.lio.exercisepracticesystem.repository;

import com.lio.exercisepracticesystem.entity.Question;
import com.lio.exercisepracticesystem.entity.QuestionType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface QuestionRepository extends JpaRepository<Question, Long> {

    List<Question> findByUserIdAndSubjectId(Long userId, Long subjectId);

    List<Question> findByUserIdAndSubjectIdAndType(Long userId, Long subjectId, QuestionType type);

    List<Question> findBySubjectId(Long subjectId);

    List<Question> findBySubjectIdAndType(Long subjectId, QuestionType type);

    List<Question> findByUserIdAndSubjectIdAndQuestion(Long userId, Long subjectId, String question);
}

