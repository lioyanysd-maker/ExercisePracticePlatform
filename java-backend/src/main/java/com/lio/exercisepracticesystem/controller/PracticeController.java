package com.lio.exercisepracticesystem.controller;

import com.lio.exercisepracticesystem.dto.*;
import com.lio.exercisepracticesystem.service.PracticeService;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/practice")
public class PracticeController {

    private final PracticeService practiceService;

    public PracticeController(PracticeService practiceService) {
        this.practiceService = practiceService;
    }

    @PostMapping("/start")
    public PracticeResponse start(@RequestBody PracticeRequest request) {
        return practiceService.startPractice(request);
    }

    @PostMapping("/submit")
    public SubmitAnswersResponse submit(@RequestBody SubmitAnswersRequest request) {
        return practiceService.submitAnswers(request);
    }

    @GetMapping("/statistics/today")
    public StatisticsResponse getTodayStatistics(@RequestParam("user_id") Long userId) {
        return practiceService.getTodayStatistics(userId);
    }

    @GetMapping("/statistics/week")
    public StatisticsResponse getWeekStatistics(@RequestParam("user_id") Long userId) {
        return practiceService.getWeekStatistics(userId);
    }

    @GetMapping("/statistics/all")
    public StatisticsResponse getAllStatistics(@RequestParam("user_id") Long userId) {
        return practiceService.getAllStatistics(userId);
    }

    @GetMapping("/statistics/home")
    public HomeStatisticsResponse getHomeStatistics(@RequestParam("user_id") Long userId) {
        return practiceService.getHomeStatistics(userId);
    }

    @GetMapping("/history")
    public Map<String, List<PracticeHistoryItem>> getHistory(
            @RequestParam("user_id") Long userId,
            @RequestParam(value = "subject_id", required = false) Long subjectId,
            @RequestParam(value = "limit", required = false) Integer limit) {
        return Map.of("records", practiceService.getPracticeHistory(userId, subjectId, limit));
    }

    @GetMapping("/session/{sessionId}/details")
    public Map<String, List<Map<String, Object>>> getSessionDetails(
            @PathVariable("sessionId") Long sessionId,
            @RequestParam("user_id") Long userId) {
        return practiceService.getSessionDetails(sessionId, userId);
    }
}

