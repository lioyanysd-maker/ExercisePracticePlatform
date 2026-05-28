package com.lio.exercisepracticesystem.service;

import com.lio.exercisepracticesystem.dto.SubjectCreate;
import com.lio.exercisepracticesystem.dto.SubjectResponse;
import com.lio.exercisepracticesystem.entity.Subject;
import com.lio.exercisepracticesystem.repository.SubjectRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class SubjectService {

    private final SubjectRepository subjectRepository;
    private final ShareService shareService;

    public SubjectService(SubjectRepository subjectRepository, ShareService shareService) {
        this.subjectRepository = subjectRepository;
        this.shareService = shareService;
    }

    @Transactional
    public SubjectResponse create(SubjectCreate request) {
        subjectRepository.findByUserIdAndName(request.getUserId(), request.getName())
                .ifPresent(s -> {
                    throw new IllegalArgumentException("该科目已存在");
                });

        Subject subject = new Subject();
        subject.setUserId(request.getUserId());
        subject.setName(request.getName());
        subject.setCreatedAt(LocalDateTime.now());

        Subject saved = subjectRepository.save(subject);
        String createdAt = saved.getCreatedAt() != null
                ? saved.getCreatedAt().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))
                : null;
        return new SubjectResponse(saved.getId(), saved.getName(), saved.getUserId(),
                createdAt, true, false, null, null, false);
    }

    public List<SubjectResponse> listByUser(Long userId) {
        return shareService.getAccessibleSubjects(userId);
    }

    public SubjectResponse get(Long subjectId, Long userId) {
        Subject subject = subjectRepository.findById(subjectId)
                .orElseThrow(() -> new IllegalArgumentException("科目不存在"));
        if (!shareService.canAccessSubject(userId, subjectId)) {
            throw new IllegalArgumentException("无权访问此科目");
        }
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        boolean isOwner = subject.getUserId().equals(userId);
        boolean hasShared = !shareService.getShareStatus(subjectId).isEmpty();
        String createdAt = subject.getCreatedAt() != null
                ? subject.getCreatedAt().format(formatter)
                : null;
        return new SubjectResponse(subject.getId(), subject.getName(), subject.getUserId(),
                createdAt, isOwner, false, null, null, hasShared);
    }

    @Transactional
    public void delete(Long subjectId, Long userId) {
        if (!shareService.canEditSubject(userId, subjectId)) {
            throw new IllegalArgumentException("无权删除此科目");
        }
        Subject subject = subjectRepository.findById(subjectId)
                .orElseThrow(() -> new IllegalArgumentException("科目不存在"));
        subjectRepository.delete(subject);
    }
}

