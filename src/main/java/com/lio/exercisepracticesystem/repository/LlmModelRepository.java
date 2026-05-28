package com.lio.exercisepracticesystem.repository;

import com.lio.exercisepracticesystem.entity.LlmModel;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface LlmModelRepository extends JpaRepository<LlmModel, Long> {

    Optional<LlmModel> findByUserId(Long userId);

    List<LlmModel> findAllByUserId(Long userId);
}
