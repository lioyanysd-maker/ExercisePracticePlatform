package com.lio.exercisepracticesystem.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.OffsetDateTime;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "error_book")
public class ErrorBook {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "subject_id", nullable = false)
    private Long subjectId;

    @Column(name = "question_id", nullable = false)
    private Long questionId;

    @Column(name = "wrong_count")
    private Integer wrongCount;

    @Column(name = "last_wrong_at")
    private OffsetDateTime lastWrongAt;
}

