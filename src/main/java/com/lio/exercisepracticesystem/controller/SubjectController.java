package com.lio.exercisepracticesystem.controller;

import com.lio.exercisepracticesystem.dto.SubjectCreate;
import com.lio.exercisepracticesystem.dto.SubjectResponse;
import com.lio.exercisepracticesystem.service.SubjectService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/subjects")
public class SubjectController {

    private final SubjectService subjectService;

    public SubjectController(SubjectService subjectService) {
        this.subjectService = subjectService;
    }

    @PostMapping
    public SubjectResponse create(@RequestBody SubjectCreate request) {
        return subjectService.create(request);
    }

    @GetMapping
    public List<SubjectResponse> list(@RequestParam("user_id") Long userId) {
        return subjectService.listByUser(userId);
    }

    @GetMapping("/{subjectId}")
    public SubjectResponse get(@PathVariable("subjectId") Long subjectId,
                               @RequestParam("user_id") Long userId) {
        return subjectService.get(subjectId, userId);
    }

    @DeleteMapping("/{subjectId}")
    public void delete(@PathVariable("subjectId") Long subjectId,
                       @RequestParam("user_id") Long userId) {
        subjectService.delete(subjectId, userId);
    }
}

