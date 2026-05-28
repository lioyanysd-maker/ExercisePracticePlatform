package com.lio.exercisepracticesystem.repository;

import com.lio.exercisepracticesystem.entity.Subject;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface SubjectRepository extends JpaRepository<Subject, Long> {

    List<Subject> findByUserId(Long userId);

    Optional<Subject> findByUserIdAndName(Long userId, String name);
}

