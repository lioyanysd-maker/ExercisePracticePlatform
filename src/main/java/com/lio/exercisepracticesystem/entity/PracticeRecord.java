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
@Table(name = "practice_records")
public class PracticeRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "session_id")
    private Long sessionId;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "subject_id", nullable = false)
    private Long subjectId;

    @Column(name = "question_id", nullable = false)
    private Long questionId;

    @Column(name = "user_answer", nullable = false, columnDefinition = "text")
    private String userAnswer;

    @Column(name = "is_correct", nullable = false)
    private Integer isCorrect;

    @Column(name = "created_at")
    private LocalDateTime createdAt;
}

