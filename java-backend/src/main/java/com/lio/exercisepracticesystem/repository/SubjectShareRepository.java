package com.lio.exercisepracticesystem.repository;

import com.lio.exercisepracticesystem.entity.SubjectShare;
import com.lio.exercisepracticesystem.entity.SubjectShare.ShareType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface SubjectShareRepository extends JpaRepository<SubjectShare, Long> {

    List<SubjectShare> findBySubjectId(Long subjectId);

    List<SubjectShare> findByOwnerUserId(Long ownerUserId);

    Optional<SubjectShare> findBySubjectIdAndTargetUserIdAndShareType(Long subjectId, Long targetUserId, ShareType shareType);

    List<SubjectShare> findBySubjectIdAndShareType(Long subjectId, ShareType shareType);
}
