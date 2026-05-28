package com.lio.exercisepracticesystem.controller;

import com.lio.exercisepracticesystem.dto.ResourceCreate;
import com.lio.exercisepracticesystem.dto.ResourceResponse;
import com.lio.exercisepracticesystem.service.ResourceService;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/resources")
public class ResourceController {

    private final ResourceService resourceService;

    public ResourceController(ResourceService resourceService) {
        this.resourceService = resourceService;
    }

    @PostMapping
    public ResourceResponse create(@RequestBody ResourceCreate request) {
        return resourceService.create(request);
    }

    @GetMapping("/question/{questionId}")
    public List<ResourceResponse> getByQuestion(@PathVariable("questionId") Long questionId) {
        return resourceService.getByQuestion(questionId);
    }

    @DeleteMapping("/{resourceId}")
    public Map<String, String> delete(@PathVariable("resourceId") Long resourceId) {
        resourceService.delete(resourceId);
        return Map.of("message", "删除成功");
    }

    @PostMapping("/upload")
    public Map<String, Object> uploadImage(@RequestParam("question_id") Long questionId,
                                           @RequestParam(value = "resource_order", required = false) Integer resourceOrder,
                                           @RequestParam("file") MultipartFile file) {
        return resourceService.uploadImage(questionId, resourceOrder, file);
    }
}
