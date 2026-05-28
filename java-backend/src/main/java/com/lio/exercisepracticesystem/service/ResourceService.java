package com.lio.exercisepracticesystem.service;

import com.lio.exercisepracticesystem.dto.ResourceCreate;
import com.lio.exercisepracticesystem.dto.ResourceResponse;
import com.lio.exercisepracticesystem.entity.Question;
import com.lio.exercisepracticesystem.entity.QuestionResource;
import com.lio.exercisepracticesystem.repository.QuestionRepository;
import com.lio.exercisepracticesystem.repository.QuestionResourceRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Base64;
import java.util.Map;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ResourceService {

    private final QuestionResourceRepository resourceRepository;
    private final QuestionRepository questionRepository;
    private final String uploadDir;

    public ResourceService(QuestionResourceRepository resourceRepository,
                          QuestionRepository questionRepository,
                          @Value("${app.upload-dir:uploads}") String uploadDir) {
        this.resourceRepository = resourceRepository;
        this.questionRepository = questionRepository;
        this.uploadDir = uploadDir;
    }

    @Transactional
    public ResourceResponse create(ResourceCreate request) {
        Question question = questionRepository.findById(request.getQuestionId())
                .orElseThrow(() -> new IllegalArgumentException("题目不存在"));

        QuestionResource resource = new QuestionResource();
        resource.setQuestionId(request.getQuestionId());
        resource.setResourceType(request.getResourceType());
        resource.setResourceContent(request.getResourceContent());
        resource.setResourceOrder(request.getResourceOrder() != null ? request.getResourceOrder() : 0);
        java.time.LocalDateTime now = java.time.LocalDateTime.now();
        resource.setCreatedAt(now);
        resource.setUpdatedAt(now);

        QuestionResource saved = resourceRepository.save(resource);
        String createdAt = saved.getCreatedAt() != null
                ? saved.getCreatedAt().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))
                : null;

        return new ResourceResponse(saved.getId(), saved.getQuestionId(), saved.getResourceType(),
                saved.getResourceContent(), saved.getResourceOrder(), createdAt);
    }

    public List<ResourceResponse> getByQuestion(Long questionId) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        return resourceRepository.findByQuestionIdOrderByResourceOrderAsc(questionId).stream()
                .map(r -> new ResourceResponse(
                        r.getId(),
                        r.getQuestionId(),
                        r.getResourceType(),
                        r.getResourceContent(),
                        r.getResourceOrder(),
                        r.getCreatedAt() != null ? r.getCreatedAt().format(formatter) : null
                ))
                .collect(Collectors.toList());
    }

    @Transactional
    public void delete(Long resourceId) {
        QuestionResource resource = resourceRepository.findById(resourceId)
                .orElseThrow(() -> new IllegalArgumentException("资源不存在"));
        resourceRepository.delete(resource);
    }

    @Transactional
    public Map<String, Object> uploadImage(Long questionId, Integer resourceOrder, MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("上传文件不能为空");
        }
        String contentType = file.getContentType() == null ? "" : file.getContentType().toLowerCase();
        if (!contentType.startsWith("image/")) {
            throw new IllegalArgumentException("仅支持图片文件上传");
        }

        Question question = questionRepository.findById(questionId)
                .orElseThrow(() -> new IllegalArgumentException("题目不存在"));

        try {
            String originalName = file.getOriginalFilename() == null ? "image" : file.getOriginalFilename();
            String ext = "";
            int dot = originalName.lastIndexOf(".");
            if (dot >= 0 && dot < originalName.length() - 1) {
                ext = originalName.substring(dot).toLowerCase();
            }
            String storedName = "q" + questionId + "_" + System.currentTimeMillis() + ext;

            Path base = Paths.get(uploadDir).toAbsolutePath().normalize();
            Path questionDir = base.resolve("questions").resolve(String.valueOf(questionId)).normalize();
            Files.createDirectories(questionDir);

            Path target = questionDir.resolve(storedName).normalize();
            Files.copy(file.getInputStream(), target, StandardCopyOption.REPLACE_EXISTING);

            String publicUrl = "/uploads/questions/" + questionId + "/" + storedName;

            QuestionResource resource = new QuestionResource();
            resource.setQuestionId(questionId);
            resource.setResourceType("image");
            resource.setResourceContent(publicUrl);
            resource.setResourceOrder(resourceOrder != null ? resourceOrder : 0);
            java.time.LocalDateTime now = java.time.LocalDateTime.now();
            resource.setCreatedAt(now);
            resource.setUpdatedAt(now);
            QuestionResource saved = resourceRepository.save(resource);

            return Map.of(
                    "resource_id", saved.getId(),
                    "question_id", question.getId(),
                    "resource_type", "image",
                    "resource_content", publicUrl
            );
        } catch (Exception e) {
            throw new RuntimeException("图片上传失败: " + e.getMessage(), e);
        }
    }

    @Transactional
    public Map<String, Object> saveImageBytes(Long questionId, Integer resourceOrder,
                                              byte[] bytes, String fileNameOrExtHint) {
        if (bytes == null || bytes.length == 0) {
            throw new IllegalArgumentException("图片内容为空");
        }
        Question question = questionRepository.findById(questionId)
                .orElseThrow(() -> new IllegalArgumentException("题目不存在"));

        try {
            String hint = fileNameOrExtHint == null ? "" : fileNameOrExtHint.toLowerCase();
            String ext = "";
            int dot = hint.lastIndexOf(".");
            if (dot >= 0 && dot < hint.length() - 1) {
                ext = hint.substring(dot);
            } else if (!hint.isBlank() && hint.length() <= 5 && !hint.contains("/")) {
                ext = "." + hint;
            }

            String storedName = "q" + questionId + "_" + System.currentTimeMillis() + ext;

            Path base = Paths.get(uploadDir).toAbsolutePath().normalize();
            Path questionDir = base.resolve("questions").resolve(String.valueOf(questionId)).normalize();
            Files.createDirectories(questionDir);

            Path target = questionDir.resolve(storedName).normalize();
            Files.write(target, bytes);

            String publicUrl = "/uploads/questions/" + questionId + "/" + storedName;

            QuestionResource resource = new QuestionResource();
            resource.setQuestionId(questionId);
            resource.setResourceType("image");
            resource.setResourceContent(publicUrl);
            resource.setResourceOrder(resourceOrder != null ? resourceOrder : 0);
            java.time.LocalDateTime now = java.time.LocalDateTime.now();
            resource.setCreatedAt(now);
            resource.setUpdatedAt(now);
            QuestionResource saved = resourceRepository.save(resource);

            return Map.of(
                    "resource_id", saved.getId(),
                    "question_id", question.getId(),
                    "resource_type", "image",
                    "resource_content", publicUrl
            );
        } catch (Exception e) {
            throw new RuntimeException("保存图片失败: " + e.getMessage(), e);
        }
    }
}
