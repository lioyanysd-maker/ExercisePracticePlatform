package com.lio.exercisepracticesystem.service;

import com.lio.exercisepracticesystem.dto.ModelCreate;
import com.lio.exercisepracticesystem.dto.ModelResponse;
import com.lio.exercisepracticesystem.dto.ModelUpdate;
import com.lio.exercisepracticesystem.entity.LlmModel;
import com.lio.exercisepracticesystem.repository.LlmModelRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ModelService {

    private final LlmModelRepository llmModelRepository;

    public ModelService(LlmModelRepository llmModelRepository) {
        this.llmModelRepository = llmModelRepository;
    }

    @Transactional
    public ModelResponse create(ModelCreate request) {
        LlmModel model = new LlmModel();
        model.setUserId(request.getUserId());
        model.setModelName(request.getModelName());
        model.setBaseUrl(request.getBaseUrl());
        model.setApiKey(request.getApiKey());
        model.setCreatedAt(java.time.LocalDateTime.now());

        LlmModel saved = llmModelRepository.save(model);
        String createdAt = saved.getCreatedAt() != null
                ? saved.getCreatedAt().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))
                : null;

        return new ModelResponse(saved.getId(), saved.getUserId(), saved.getModelName(),
                saved.getBaseUrl(), saved.getApiKey(), createdAt);
    }

    public List<ModelResponse> listByUser(Long userId) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        return llmModelRepository.findAllByUserId(userId).stream()
                .map(m -> new ModelResponse(
                        m.getId(),
                        m.getUserId(),
                        m.getModelName(),
                        m.getBaseUrl(),
                        m.getApiKey(),
                        m.getCreatedAt() != null ? m.getCreatedAt().format(formatter) : null
                ))
                .collect(Collectors.toList());
    }

    public ModelResponse get(Long modelId) {
        LlmModel model = llmModelRepository.findById(modelId)
                .orElseThrow(() -> new IllegalArgumentException("模型配置不存在"));

        String createdAt = model.getCreatedAt() != null
                ? model.getCreatedAt().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))
                : null;

        return new ModelResponse(model.getId(), model.getUserId(), model.getModelName(),
                model.getBaseUrl(), model.getApiKey(), createdAt);
    }

    @Transactional
    public ModelResponse update(Long modelId, ModelUpdate request) {
        LlmModel model = llmModelRepository.findById(modelId)
                .orElseThrow(() -> new IllegalArgumentException("模型配置不存在"));

        model.setModelName(request.getModelName());
        model.setBaseUrl(request.getBaseUrl());
        model.setApiKey(request.getApiKey());

        LlmModel saved = llmModelRepository.save(model);
        String createdAt = saved.getCreatedAt() != null
                ? saved.getCreatedAt().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))
                : null;

        return new ModelResponse(saved.getId(), saved.getUserId(), saved.getModelName(),
                saved.getBaseUrl(), saved.getApiKey(), createdAt);
    }

    @Transactional
    public void delete(Long modelId) {
        LlmModel model = llmModelRepository.findById(modelId)
                .orElseThrow(() -> new IllegalArgumentException("模型配置不存在"));
        llmModelRepository.delete(model);
    }
}
