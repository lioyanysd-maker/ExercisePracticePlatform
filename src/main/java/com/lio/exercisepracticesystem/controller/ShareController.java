package com.lio.exercisepracticesystem.controller;

import com.lio.exercisepracticesystem.dto.SetShareRequest;
import com.lio.exercisepracticesystem.dto.ShareResponse;
import com.lio.exercisepracticesystem.service.ShareService;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/shares")
public class ShareController {

    private final ShareService shareService;

    public ShareController(ShareService shareService) {
        this.shareService = shareService;
    }

    @PostMapping
    public ShareResponse setShare(@RequestBody SetShareRequest request) {
        return shareService.setShare(request.getOwnerUserId(), request.getSubjectId(),
                request.getTargetUserId(), request.getShareType());
    }

    @DeleteMapping("/{subjectId}")
    public Map<String, String> cancelShare(@PathVariable("subjectId") Long subjectId,
                                           @RequestParam("owner_user_id") Long ownerUserId,
                                           @RequestParam(value = "target_user_id", required = false) Long targetUserId,
                                           @RequestParam(value = "share_type", required = false) String shareType) {
        return shareService.cancelShare(ownerUserId, subjectId, targetUserId, shareType);
    }

    @GetMapping("/status/{subjectId}")
    public List<ShareResponse> getShareStatus(@PathVariable("subjectId") Long subjectId) {
        return shareService.getShareStatus(subjectId);
    }

    @GetMapping("/my-shared")
    public List<Map<String, Object>> getMyShared(@RequestParam("user_id") Long userId) {
        return shareService.getMySharedSubjects(userId);
    }

    @GetMapping("/users/search")
    public List<Map<String, Object>> searchUsers(@RequestParam("keyword") String keyword,
                                                  @RequestParam("current_user_id") Long currentUserId,
                                                  @RequestParam(value = "limit", defaultValue = "10") Integer limit) {
        return shareService.searchUsers(keyword, currentUserId, limit);
    }
}
