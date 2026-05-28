package com.lio.exercisepracticesystem.controller;

import com.lio.exercisepracticesystem.dto.QuestionCreate;
import com.lio.exercisepracticesystem.dto.QuestionResponse;
import com.lio.exercisepracticesystem.dto.QuestionUpdate;
import com.lio.exercisepracticesystem.service.QuestionService;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/questions")
public class QuestionController {

    private final QuestionService questionService;

    public QuestionController(QuestionService questionService) {
        this.questionService = questionService;
    }

    @PostMapping
    public QuestionResponse create(@RequestBody QuestionCreate request) {
        return questionService.create(request);
    }

    @GetMapping
    public List<QuestionResponse> list(@RequestParam("user_id") Long userId,
                                       @RequestParam("subject_id") Long subjectId,
                                       @RequestParam(value = "question_type", required = false) String type,
                                       @RequestParam(value = "limit", required = false) Integer limit) {
        return questionService.list(userId, subjectId, type, limit);
    }

    @GetMapping("/types/{subjectId}")
    public Map<String, List<String>> getTypes(@PathVariable("subjectId") Long subjectId,
                                              @RequestParam("user_id") Long userId) {
        return Map.of("types", questionService.getQuestionTypesBySubject(subjectId));
    }

    @GetMapping("/{questionId}")
    public QuestionResponse get(@PathVariable("questionId") Long questionId,
                                @RequestParam("user_id") Long userId) {
        return questionService.get(questionId, userId);
    }

    @PutMapping("/{questionId}")
    public QuestionResponse update(@PathVariable("questionId") Long questionId,
                                   @RequestParam("user_id") Long userId,
                                   @RequestBody QuestionUpdate request) {
        return questionService.update(questionId, userId, request);
    }

    @DeleteMapping("/{questionId}")
    public void delete(@PathVariable("questionId") Long questionId,
                       @RequestParam("user_id") Long userId) {
        questionService.delete(questionId, userId);
    }
}
