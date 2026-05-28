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
@Table(name = "subject_shares")
public class SubjectShare {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "owner_user_id", nullable = false)
    private Long ownerUserId;

    @Column(name = "subject_id", nullable = false)
    private Long subjectId;

    @Column(name = "target_user_id")
    private Long targetUserId;

    @Enumerated(EnumType.STRING)
    @Column(name = "share_type", nullable = false, length = 20)
    private ShareType shareType;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    public enum ShareType {
        USER,
        PUBLIC
    }
}
