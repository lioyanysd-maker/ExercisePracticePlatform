package com.lio.exercisepracticesystem.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "practice_sessions")
public class PracticeSession {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "subject_id", nullable = false)
    private Long subjectId;

    @Column(name = "total_count", nullable = false)
    private Integer totalCount;

    @Column(name = "correct_count", nullable = false)
    private Integer correctCount;

    @Column(name = "wrong_count", nullable = false)
    private Integer wrongCount;

    @Column(name = "accuracy", nullable = false, length = 10)
    private String accuracy;

    @Column(name = "grade", nullable = false, length = 10)
    private String grade;

    @Column(name = "created_at")
    private LocalDateTime createdAt;
}

