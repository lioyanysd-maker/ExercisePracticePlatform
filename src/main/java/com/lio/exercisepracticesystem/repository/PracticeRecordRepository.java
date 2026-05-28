package com.lio.exercisepracticesystem.repository;

import com.lio.exercisepracticesystem.entity.PracticeRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface PracticeRecordRepository extends JpaRepository<PracticeRecord, Long> {

    List<PracticeRecord> findByUserId(Long userId);

    List<PracticeRecord> findBySessionIdOrderById(Long sessionId);

    List<PracticeRecord> findByUserIdAndCreatedAtAfter(Long userId, LocalDateTime after);


    @Query("SELECT COUNT(pr) FROM PracticeRecord pr WHERE pr.userId = :userId")
    long countByUserId(@Param("userId") Long userId);

    @Query("SELECT COUNT(pr) FROM PracticeRecord pr WHERE pr.userId = :userId AND pr.createdAt >= :after")
    long countByUserIdAndCreatedAtAfter(@Param("userId") Long userId, @Param("after") LocalDateTime after);

    @Query("SELECT COUNT(pr) FROM PracticeRecord pr WHERE pr.userId = :userId AND pr.isCorrect = 1")
    long countCorrectByUserId(@Param("userId") Long userId);

    @Query("SELECT COUNT(pr) FROM PracticeRecord pr WHERE pr.userId = :userId AND pr.isCorrect = 1 AND pr.createdAt >= :after")
    long countCorrectByUserIdAndCreatedAtAfter(@Param("userId") Long userId, @Param("after") LocalDateTime after);
}

