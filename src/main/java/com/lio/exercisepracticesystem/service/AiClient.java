package com.lio.exercisepracticesystem.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

@Service
public class AiClient {

    private final WebClient webClient;
    private final ObjectMapper objectMapper;

    public AiClient(ObjectMapper objectMapper) {
        this.webClient = WebClient.builder().build();
        this.objectMapper = objectMapper;
    }

    private String buildChatCompletionsUrl(String baseUrl) {
        if (baseUrl == null || baseUrl.isBlank()) {
            throw new IllegalArgumentException("AI API base URL 不能为空");
        }
        String url = baseUrl.trim();
        if (url.endsWith("/")) {
            url = url.substring(0, url.length() - 1);
        }
        if (url.contains("chat.deepseek.com")) {
            url = url.replace("chat.deepseek.com", "api.deepseek.com");
        }
        if (url.endsWith("/v1")) {
            return url + "/chat/completions";
        }
        return url + "/v1/chat/completions";
    }

    private static final Set<String> DEEPSEEK_KNOWN_MODELS = Set.of(
            "deepseek-chat", "deepseek-reasoner", "deepseek-coder"
    );

    private String normalizeModelName(String baseUrl, String modelName) {
        if (baseUrl == null || !baseUrl.contains("deepseek.com")) {
            return (modelName != null && !modelName.isBlank()) ? modelName.trim() : (modelName != null ? modelName : "");
        }
        if (modelName == null || modelName.isBlank()) {
            return "deepseek-chat";
        }
        String trimmed = modelName.trim();
        if (DEEPSEEK_KNOWN_MODELS.contains(trimmed)) {
            return trimmed;
        }
        return "deepseek-chat";
    }

    public String chatCompletion(String baseUrl, String apiKey, String modelName,
                                 List<Map<String, Object>> messages, Double temperature,
                                 Integer maxTokens, boolean jsonMode) {
        try {
            String model = normalizeModelName(baseUrl, modelName);
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("model", model);
            requestBody.put("messages", messages);
            if (temperature != null) {
                requestBody.put("temperature", temperature);
            }
            int effectiveMaxTokens = (maxTokens != null && maxTokens > 0) ? maxTokens : 4096;
            requestBody.put("max_tokens", effectiveMaxTokens);
            if (jsonMode) {
                requestBody.put("response_format", Map.of("type", "json_object"));
            }

            String completionsUrl = buildChatCompletionsUrl(baseUrl);
            String response = webClient.post()
                    .uri(completionsUrl)
                    .header("Authorization", "Bearer " + apiKey)
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(requestBody)
                    .retrieve()
                    .onStatus(HttpStatusCode::isError, resp -> resp.bodyToMono(String.class)
                            .flatMap(body -> Mono.error(new RuntimeException(
                                    "AI API 返回 " + resp.statusCode() + ": " + body))))
                    .bodyToMono(String.class)
                    .block();

            if (response == null) {
                throw new RuntimeException("AI API 返回为空");
            }

            JsonNode jsonNode = objectMapper.readTree(response);
            return jsonNode.path("choices").get(0).path("message").path("content").asText();
        } catch (Exception e) {
            throw new RuntimeException("调用 AI API 失败: " + e.getMessage(), e);
        }
    }

    public String parseTextToQuestions(String baseUrl, String apiKey, String modelName,
                                       String text, String subject) {
        List<Map<String, Object>> messages = new ArrayList<>();
        messages.add(Map.of("role", "system", "content", com.lio.exercisepracticesystem.util.PromptTemplates.SYSTEM_ROLE_PARSE_TEXT));
        messages.add(Map.of("role", "user", "content", com.lio.exercisepracticesystem.util.PromptTemplates.getParseTextPrompt(text, subject)));

        return chatCompletion(baseUrl, apiKey, modelName, messages, 0.3, null, true);
    }

    public String parseImageToQuestions(String baseUrl, String apiKey, String modelName,
                                        String imageText, String subject) {
        List<Map<String, Object>> messages = new ArrayList<>();
        messages.add(Map.of("role", "system", "content", com.lio.exercisepracticesystem.util.PromptTemplates.SYSTEM_ROLE_PARSE_IMAGE));
        messages.add(Map.of("role", "user", "content", com.lio.exercisepracticesystem.util.PromptTemplates.getParseImagePrompt(imageText, subject)));

        return chatCompletion(baseUrl, apiKey, modelName, messages, 0.3, null, true);
    }

    public String parseImageDirect(String baseUrl, String apiKey, String modelName,
                                    Path imagePath, String subject) {
        try {
            byte[] imageBytes = Files.readAllBytes(imagePath);
            String base64Image = Base64.getEncoder().encodeToString(imageBytes);
            String mimeType = "image/jpeg";
            String filename = imagePath.getFileName().toString().toLowerCase();
            if (filename.endsWith(".png")) {
                mimeType = "image/png";
            } else if (filename.endsWith(".jpg") || filename.endsWith(".jpeg")) {
                mimeType = "image/jpeg";
            }

            List<Map<String, Object>> content = new ArrayList<>();
            content.add(Map.of(
                    "type", "image_url",
                    "image_url", Map.of("url", "data:" + mimeType + ";base64," + base64Image)
            ));
            content.add(Map.of(
                    "type", "text",
                    "text", com.lio.exercisepracticesystem.util.PromptTemplates.getParseImageDirectPrompt(subject)
            ));

            List<Map<String, Object>> messages = new ArrayList<>();
            messages.add(Map.of("role", "user", "content", content));

            return chatCompletion(baseUrl, apiKey, modelName, messages, 0.3, null, true);
        } catch (Exception e) {
            throw new RuntimeException("解析图片失败: " + e.getMessage(), e);
        }
    }

    public String extractTextFromImage(String baseUrl, String apiKey, String modelName,
                                       Path imagePath) {
        try {
            byte[] imageBytes = Files.readAllBytes(imagePath);
            String base64Image = Base64.getEncoder().encodeToString(imageBytes);
            String mimeType = "image/jpeg";
            String filename = imagePath.getFileName().toString().toLowerCase();
            if (filename.endsWith(".png")) {
                mimeType = "image/png";
            } else if (filename.endsWith(".jpg") || filename.endsWith(".jpeg")) {
                mimeType = "image/jpeg";
            }

            List<Map<String, Object>> content = new ArrayList<>();
            content.add(Map.of(
                    "type", "image_url",
                    "image_url", Map.of("url", "data:" + mimeType + ";base64," + base64Image)
            ));
            content.add(Map.of(
                    "type", "text",
                    "text", "请将这张图片中的所有文字内容完整提取出来。如果有数学公式，请使用 LaTeX 格式。保持原有排版结构。不要包含任何解释性文字，只返回提取的文本内容。"
            ));

            List<Map<String, Object>> messages = new ArrayList<>();
            messages.add(Map.of("role", "user", "content", content));

            return chatCompletion(baseUrl, apiKey, modelName, messages, 0.1, null, false);
        } catch (Exception e) {
            throw new RuntimeException("提取图片文本失败: " + e.getMessage(), e);
        }
    }
}
