package com.lio.exercisepracticesystem.controller;

import com.lio.exercisepracticesystem.dto.ErrorQuestionResponse;
import com.lio.exercisepracticesystem.dto.PracticeRequest;
import com.lio.exercisepracticesystem.dto.PracticeResponse;
import com.lio.exercisepracticesystem.service.ErrorService;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/errors")
public class ErrorController {

    private final ErrorService errorService;

    public ErrorController(ErrorService errorService) {
        this.errorService = errorService;
    }

    @GetMapping
    public List<ErrorQuestionResponse> list(@RequestParam("user_id") Long userId,
                                            @RequestParam(value = "subject_id", required = false) Long subjectId,
                                            @RequestParam(value = "limit", required = false) Integer limit) {
        return errorService.getErrorQuestions(userId, subjectId, limit);
    }

    @GetMapping("/count")
    public Map<String, Long> count(@RequestParam("user_id") Long userId,
                                    @RequestParam(value = "subject_id", required = false) Long subjectId) {
        return Map.of("count", errorService.getErrorCount(userId, subjectId));
    }

    @GetMapping("/types/{subjectId}")
    public Map<String, List<String>> getTypes(@PathVariable("subjectId") Long subjectId,
                                              @RequestParam("user_id") Long userId) {
        return Map.of("types", errorService.getErrorTypesBySubject(userId, subjectId));
    }

    @PostMapping("/practice")
    public PracticeResponse practice(@RequestBody PracticeRequest request) {
        return errorService.startErrorPractice(request);
    }

    @DeleteMapping("/{errorId}")
    public Map<String, String> remove(@PathVariable("errorId") Long errorId,
                                      @RequestParam("user_id") Long userId) {
        boolean success = errorService.removeError(errorId, userId);
        if (!success) {
            throw new IllegalArgumentException("错题记录不存在或无权删除");
        }
        return Map.of("message", "错题移除成功");
    }
}
