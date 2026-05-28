package com.lio.exercisepracticesystem.service;

import com.lio.exercisepracticesystem.dto.ShareResponse;
import com.lio.exercisepracticesystem.entity.Subject;
import com.lio.exercisepracticesystem.entity.SubjectShare;
import com.lio.exercisepracticesystem.entity.User;
import com.lio.exercisepracticesystem.entity.SubjectShare.ShareType;
import com.lio.exercisepracticesystem.repository.SubjectRepository;
import com.lio.exercisepracticesystem.repository.SubjectShareRepository;
import com.lio.exercisepracticesystem.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class ShareService {

    private final SubjectShareRepository subjectShareRepository;
    private final SubjectRepository subjectRepository;
    private final UserRepository userRepository;

    public ShareService(SubjectShareRepository subjectShareRepository,
                       SubjectRepository subjectRepository,
                       UserRepository userRepository) {
        this.subjectShareRepository = subjectShareRepository;
        this.subjectRepository = subjectRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    public ShareResponse setShare(Long ownerUserId, Long subjectId, Long targetUserId, String shareTypeStr) {
        Subject subject = subjectRepository.findById(subjectId)
                .orElseThrow(() -> new IllegalArgumentException("科目不存在"));
        if (!subject.getUserId().equals(ownerUserId)) {
            throw new IllegalArgumentException("无权设置此科目的共享");
        }

        ShareType shareType = ShareType.valueOf(shareTypeStr.toUpperCase());
        if (shareType == ShareType.USER && targetUserId == null) {
            throw new IllegalArgumentException("指定用户共享时必须提供目标用户ID");
        }

        SubjectShare share = new SubjectShare();
        share.setOwnerUserId(ownerUserId);
        share.setSubjectId(subjectId);
        share.setTargetUserId(targetUserId);
        share.setShareType(shareType);
        share.setCreatedAt(java.time.LocalDateTime.now());

        SubjectShare saved = subjectShareRepository.save(share);

        String targetUsername = null;
        if (targetUserId != null) {
            User targetUser = userRepository.findById(targetUserId).orElse(null);
            if (targetUser != null) {
                targetUsername = targetUser.getUsername();
            }
        }

        String createdAt = saved.getCreatedAt() != null
                ? saved.getCreatedAt().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))
                : null;

        return new ShareResponse(saved.getId(), saved.getSubjectId(), saved.getTargetUserId(),
                targetUsername, saved.getShareType().name(), createdAt);
    }

    @Transactional
    public Map<String, String> cancelShare(Long ownerUserId, Long subjectId, Long targetUserId, String shareTypeStr) {
        Subject subject = subjectRepository.findById(subjectId)
                .orElseThrow(() -> new IllegalArgumentException("科目不存在"));
        if (!subject.getUserId().equals(ownerUserId)) {
            throw new IllegalArgumentException("无权取消此科目的共享");
        }

        List<SubjectShare> shares;
        if (targetUserId != null && shareTypeStr != null) {
            ShareType shareType = ShareType.valueOf(shareTypeStr.toUpperCase());
            shares = subjectShareRepository.findBySubjectIdAndTargetUserIdAndShareType(subjectId, targetUserId, shareType)
                    .stream().collect(Collectors.toList());
        } else if (shareTypeStr != null) {
            ShareType shareType = ShareType.valueOf(shareTypeStr.toUpperCase());
            shares = subjectShareRepository.findBySubjectIdAndShareType(subjectId, shareType);
        } else {
            shares = subjectShareRepository.findBySubjectId(subjectId);
        }

        if (shares.isEmpty()) {
            throw new IllegalArgumentException("未找到匹配的共享记录");
        }

        subjectShareRepository.deleteAll(shares);
        return Map.of("message", "共享已取消");
    }

    public List<ShareResponse> getShareStatus(Long subjectId) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        return subjectShareRepository.findBySubjectId(subjectId).stream()
                .map(share -> {
                    String targetUsername = null;
                    if (share.getTargetUserId() != null) {
                        User targetUser = userRepository.findById(share.getTargetUserId()).orElse(null);
                        if (targetUser != null) {
                            targetUsername = targetUser.getUsername();
                        }
                    }
                    String createdAt = share.getCreatedAt() != null
                            ? share.getCreatedAt().format(formatter)
                            : null;
                    return new ShareResponse(share.getId(), share.getSubjectId(), share.getTargetUserId(),
                            targetUsername, share.getShareType().name(), createdAt);
                })
                .collect(Collectors.toList());
    }

    public List<Map<String, Object>> getMySharedSubjects(Long userId) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        return subjectShareRepository.findByOwnerUserId(userId).stream()
                .map(share -> {
                    Subject subject = subjectRepository.findById(share.getSubjectId()).orElse(null);
                    String subjectName = subject != null ? subject.getName() : "未知科目";
                    String createdAt = share.getCreatedAt() != null
                            ? share.getCreatedAt().format(formatter)
                            : null;
                    return Map.<String, Object>of(
                            "id", share.getId(),
                            "subject_id", share.getSubjectId(),
                            "subject_name", subjectName,
                            "target_user_id", share.getTargetUserId() != null ? share.getTargetUserId() : "",
                            "share_type", share.getShareType().name(),
                            "created_at", createdAt
                    );
                })
                .collect(Collectors.toList());
    }

    public List<Map<String, Object>> searchUsers(String keyword, Long currentUserId, Integer limit) {
        int actualLimit = limit != null && limit > 0 ? Math.min(limit, 50) : 10;
        return userRepository.findAll().stream()
                .filter(u -> u.getUsername().contains(keyword) && !u.getId().equals(currentUserId))
                .limit(actualLimit)
                .map(u -> Map.<String, Object>of("id", u.getId(), "username", u.getUsername()))
                .collect(Collectors.toList());
    }

    public boolean canAccessSubject(Long userId, Long subjectId) {
        Subject subject = subjectRepository.findById(subjectId).orElse(null);
        if (subject == null) {
            return false;
        }
        if (subject.getUserId().equals(userId)) {
            return true;
        }
        List<SubjectShare> shares = subjectShareRepository.findBySubjectId(subjectId);
        return shares.stream().anyMatch(share ->
                (share.getShareType() == ShareType.PUBLIC) ||
                (share.getShareType() == ShareType.USER && share.getTargetUserId() != null && share.getTargetUserId().equals(userId))
        );
    }

    public boolean canEditSubject(Long userId, Long subjectId) {
        Subject subject = subjectRepository.findById(subjectId).orElse(null);
        return subject != null && subject.getUserId().equals(userId);
    }

    public List<com.lio.exercisepracticesystem.dto.SubjectResponse> getAccessibleSubjects(Long userId) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        List<com.lio.exercisepracticesystem.dto.SubjectResponse> result = new java.util.ArrayList<>();

        List<Subject> ownSubjects = subjectRepository.findByUserId(userId);
        for (Subject subject : ownSubjects) {
            boolean hasShared = !subjectShareRepository.findBySubjectId(subject.getId()).isEmpty();
            result.add(new com.lio.exercisepracticesystem.dto.SubjectResponse(
                    subject.getId(),
                    subject.getName(),
                    subject.getUserId(),
                    subject.getCreatedAt() != null ? subject.getCreatedAt().format(formatter) : null,
                    true,
                    false,
                    null,
                    null,
                    hasShared
            ));
        }

        List<SubjectShare> userShares = subjectShareRepository.findAll().stream()
                .filter(s -> s.getShareType() == ShareType.USER
                        && s.getTargetUserId() != null
                        && s.getTargetUserId().equals(userId))
                .collect(Collectors.toList());

        for (SubjectShare share : userShares) {
            Subject subject = subjectRepository.findById(share.getSubjectId()).orElse(null);
            if (subject == null || subject.getUserId().equals(userId)) continue;
            User owner = userRepository.findById(subject.getUserId()).orElse(null);
            result.add(new com.lio.exercisepracticesystem.dto.SubjectResponse(
                    subject.getId(),
                    subject.getName(),
                    subject.getUserId(),
                    subject.getCreatedAt() != null ? subject.getCreatedAt().format(formatter) : null,
                    false,
                    true,
                    owner != null ? owner.getUsername() : null,
                    "USER",
                    false
            ));
        }

        List<SubjectShare> publicShares = subjectShareRepository.findAll().stream()
                .filter(s -> s.getShareType() == ShareType.PUBLIC)
                .collect(Collectors.toList());

        for (SubjectShare share : publicShares) {
            Subject subject = subjectRepository.findById(share.getSubjectId()).orElse(null);
            if (subject == null || subject.getUserId().equals(userId)) continue;
            User owner = userRepository.findById(subject.getUserId()).orElse(null);
            result.add(new com.lio.exercisepracticesystem.dto.SubjectResponse(
                    subject.getId(),
                    subject.getName(),
                    subject.getUserId(),
                    subject.getCreatedAt() != null ? subject.getCreatedAt().format(formatter) : null,
                    false,
                    true,
                    owner != null ? owner.getUsername() : null,
                    "PUBLIC",
                    false
            ));
        }

        return result;
    }
}
