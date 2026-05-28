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
@Table(name = "questions")
public class Question {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "subject_id", nullable = false)
    private Long subjectId;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private QuestionType type;

    @Column(columnDefinition = "text", nullable = false)
    private String question;

    @Column(name = "options_json", columnDefinition = "json")
    private String optionsJson;

    @Column(columnDefinition = "text", nullable = false)
    private String answer;

    @Column(columnDefinition = "text", nullable = false)
    private String analysis;

    @Column(name = "created_at")
    private LocalDateTime createdAt;
}

