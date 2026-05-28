package com.lio.exercisepracticesystem.repository;

import com.lio.exercisepracticesystem.entity.QuestionResource;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface QuestionResourceRepository extends JpaRepository<QuestionResource, Long> {

    List<QuestionResource> findByQuestionIdOrderByResourceOrderAsc(Long questionId);
}
