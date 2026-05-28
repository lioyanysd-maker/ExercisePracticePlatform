package com.lio.exercisepracticesystem.controller;

import com.lio.exercisepracticesystem.dto.ModelCreate;
import com.lio.exercisepracticesystem.dto.ModelResponse;
import com.lio.exercisepracticesystem.dto.ModelUpdate;
import com.lio.exercisepracticesystem.service.ModelService;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/models")
public class ModelController {

    private final ModelService modelService;

    public ModelController(ModelService modelService) {
        this.modelService = modelService;
    }

    @PostMapping
    public ModelResponse create(@RequestBody ModelCreate request) {
        return modelService.create(request);
    }

    @GetMapping
    public List<ModelResponse> list(@RequestParam("user_id") Long userId) {
        return modelService.listByUser(userId);
    }

    @GetMapping("/{modelId}")
    public ModelResponse get(@PathVariable("modelId") Long modelId) {
        return modelService.get(modelId);
    }

    @PutMapping("/{modelId}")
    public ModelResponse update(@PathVariable("modelId") Long modelId,
                                @RequestBody ModelUpdate request) {
        return modelService.update(modelId, request);
    }

    @DeleteMapping("/{modelId}")
    public Map<String, String> delete(@PathVariable("modelId") Long modelId) {
        modelService.delete(modelId);
        return Map.of("message", "模型配置删除成功");
    }
}
