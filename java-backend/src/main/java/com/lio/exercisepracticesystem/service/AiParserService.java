package com.lio.exercisepracticesystem.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.lio.exercisepracticesystem.entity.LlmModel;
import com.lio.exercisepracticesystem.entity.Question;
import com.lio.exercisepracticesystem.entity.QuestionType;
import com.lio.exercisepracticesystem.repository.LlmModelRepository;
import org.springframework.stereotype.Service;

import java.nio.file.Path;
import java.util.*;

@Service
public class AiParserService {

    private final AiClient aiClient;
    private final FileReaderService fileReaderService;
    private final LlmModelRepository llmModelRepository;
    private final ObjectMapper objectMapper;

    private static final int MAX_TEXT_LENGTH_FOR_AI = 8000;

    public AiParserService(AiClient aiClient, FileReaderService fileReaderService,
                           LlmModelRepository llmModelRepository, ObjectMapper objectMapper) {
        this.aiClient = aiClient;
        this.fileReaderService = fileReaderService;
        this.llmModelRepository = llmModelRepository;
        this.objectMapper = objectMapper;
    }

    private String truncateForAi(String raw) {
        if (raw == null) {
            return null;
        }
        String text = raw.trim();
        if (text.length() <= MAX_TEXT_LENGTH_FOR_AI) {
            return text;
        }
        return text.substring(0, MAX_TEXT_LENGTH_FOR_AI);
    }

    public Map<String, Object> parseFileToQuestions(Long userId, Path filePath, String subject) {
        LlmModel llmModel = llmModelRepository.findByUserId(userId)
                .orElseThrow(() -> new IllegalArgumentException("请先配置 AI 模型"));

        String textContent = fileReaderService.readFile(filePath);
        if (textContent == null || textContent.trim().isEmpty()) {
            throw new IllegalArgumentException("文件内容为空");
        }

        String truncated = truncateForAi(textContent);

        String jsonResult = aiClient.parseTextToQuestions(
                llmModel.getBaseUrl(),
                llmModel.getApiKey(),
                llmModel.getModelName(),
                truncated,
                subject
        );

        return parseAndValidate(jsonResult);
    }

    public Map<String, Object> parseImageToQuestions(Long userId, Path imagePath, String subject) {
        LlmModel llmModel = llmModelRepository.findByUserId(userId)
                .orElseThrow(() -> new IllegalArgumentException("请先配置 AI 模型"));

        try {
            String jsonResult = aiClient.parseImageDirect(
                    llmModel.getBaseUrl(),
                    llmModel.getApiKey(),
                    llmModel.getModelName(),
                    imagePath,
                    subject
            );
            return parseAndValidate(jsonResult);
        } catch (Exception e) {
            String imageText = aiClient.extractTextFromImage(
                    llmModel.getBaseUrl(),
                    llmModel.getApiKey(),
                    llmModel.getModelName(),
                    imagePath
            );
            String jsonResult = aiClient.parseImageToQuestions(
                    llmModel.getBaseUrl(),
                    llmModel.getApiKey(),
                    llmModel.getModelName(),
                    imageText,
                    subject
            );
            return parseAndValidate(jsonResult);
        }
    }

    public Map<String, Object> parseTextToQuestions(Long userId, String text, String subject) {
        LlmModel llmModel = llmModelRepository.findByUserId(userId)
                .orElseThrow(() -> new IllegalArgumentException("请先配置 AI 模型"));

        String truncated = truncateForAi(text);

        String jsonResult = aiClient.parseTextToQuestions(
                llmModel.getBaseUrl(),
                llmModel.getApiKey(),
                llmModel.getModelName(),
                truncated,
                subject
        );

        return parseAndValidate(jsonResult);
    }

    private Map<String, Object> parseAndValidate(String jsonResult) {
        try {
            JsonNode rootNode = objectMapper.readTree(jsonResult);
            JsonNode questionsNode = rootNode.path("questions");

            List<Map<String, Object>> questions = new ArrayList<>();
            if (questionsNode.isArray()) {
                for (JsonNode questionNode : questionsNode) {
                    Map<String, Object> question = new HashMap<>();
                    question.put("type", questionNode.path("type").asText());
                    question.put("question", questionNode.path("question").asText());
                    question.put("answer", questionNode.path("answer").asText());
                    question.put("analysis", questionNode.path("analysis").asText());

                    List<String> options = new ArrayList<>();
                    JsonNode optionsNode = questionNode.path("options");
                    if (optionsNode.isArray()) {
                        for (JsonNode option : optionsNode) {
                            options.add(option.asText());
                        }
                    }
                    question.put("options", options);

                    questions.add(question);
                }
            }

            Map<String, Object> result = new HashMap<>();
            result.put("questions", questions);
            return result;
        } catch (Exception e) {
            throw new RuntimeException("解析 AI 返回的 JSON 失败: " + e.getMessage(), e);
        }
    }
}
