package com.lio.exercisepracticesystem.repository;

import com.lio.exercisepracticesystem.entity.PracticeSession;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface PracticeSessionRepository extends JpaRepository<PracticeSession, Long> {

    List<PracticeSession> findByUserIdOrderByCreatedAtDesc(Long userId);

    List<PracticeSession> findByUserIdAndSubjectIdOrderByCreatedAtDesc(Long userId, Long subjectId);

    @Query("SELECT ps FROM PracticeSession ps WHERE ps.userId = :userId ORDER BY ps.createdAt DESC")
    List<PracticeSession> findByUserIdWithLimit(@Param("userId") Long userId, org.springframework.data.domain.Pageable pageable);

    List<PracticeSession> findTop15ByOrderByCreatedAtDesc();

    @Query(value = "SELECT COUNT(*) FROM practice_sessions WHERE DATE(created_at) = CURDATE()", nativeQuery = true)
    long countTodaySessions();

    @Query(value = "SELECT COUNT(DISTINCT user_id) FROM practice_sessions WHERE DATE(created_at) = CURDATE()", nativeQuery = true)
    long countTodayActiveUsers();

    @Query(value = "SELECT DATE(created_at) AS d, COUNT(*) FROM practice_sessions WHERE created_at >= :from "
            + "GROUP BY DATE(created_at) ORDER BY d ASC", nativeQuery = true)
    List<Object[]> countPracticeGroupedByDaySince(@Param("from") LocalDateTime from);
}

