package com.lio.exercisepracticesystem.controller;

import com.lio.exercisepracticesystem.dto.ImportResponse;
import com.lio.exercisepracticesystem.dto.DocumentPreviewResponse;
import com.lio.exercisepracticesystem.dto.BindDocImagesRequest;
import com.lio.exercisepracticesystem.dto.DocAnalyzeConfirmRequest;
import com.lio.exercisepracticesystem.dto.DocumentImageItem;
import com.lio.exercisepracticesystem.dto.TextImportRequest;
import com.lio.exercisepracticesystem.entity.Subject;
import com.lio.exercisepracticesystem.entity.Question;
import com.lio.exercisepracticesystem.repository.SubjectRepository;
import com.lio.exercisepracticesystem.repository.QuestionRepository;
import com.lio.exercisepracticesystem.service.AiParserService;
import com.lio.exercisepracticesystem.service.DocxImageExtractService;
import com.lio.exercisepracticesystem.service.QuestionService;
import com.lio.exercisepracticesystem.service.ResourceService;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Base64;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Pattern;

@RestController
@RequestMapping("/api/import")
public class ImportController {

    private final AiParserService aiParserService;
    private final QuestionService questionService;
    private final SubjectRepository subjectRepository;
    private final DocxImageExtractService docxImageExtractService;
    private final QuestionRepository questionRepository;
    private final ResourceService resourceService;

    public ImportController(AiParserService aiParserService,
                           QuestionService questionService,
                           SubjectRepository subjectRepository,
                           DocxImageExtractService docxImageExtractService,
                           QuestionRepository questionRepository,
                           ResourceService resourceService) {
        this.aiParserService = aiParserService;
        this.questionService = questionService;
        this.subjectRepository = subjectRepository;
        this.docxImageExtractService = docxImageExtractService;
        this.questionRepository = questionRepository;
        this.resourceService = resourceService;
    }

    @PostMapping("/doc/preview")
    public DocumentPreviewResponse previewDocImages(
            @RequestParam("user_id") Long userId,
            @RequestParam("subject_id") Long subjectId,
            @RequestParam("file") MultipartFile file) {
        Subject subject = subjectRepository.findById(subjectId)
                .orElseThrow(() -> new IllegalArgumentException("科目不存在"));
        if (!subject.getUserId().equals(userId)) {
            throw new IllegalArgumentException("无权访问此科目");
        }

        String name = file.getOriginalFilename() == null ? "" : file.getOriginalFilename().toLowerCase();
        boolean isDocx = name.endsWith(".docx");
        boolean isDoc = name.endsWith(".doc");
        if (!isDocx && !isDoc) {
            throw new IllegalArgumentException("当前预览功能仅支持 .docx / .doc（后续可扩展 PDF）");
        }

        try {
            DocxImageExtractService.DocxExtractResult r = isDocx
                    ? docxImageExtractService.extract(file.getBytes())
                    : docxImageExtractService.extractDoc(file.getInputStream());
            String msg = "提取到 " + r.getImages().size() + " 张图片，已按题号做初步匹配（可在前端调整）";
            return new DocumentPreviewResponse(
                    true,
                    msg,
                    isDocx ? "docx" : "doc",
                    r.getImages().size(),
                    r.getQuestionNumbers(),
                    r.getImages()
            );
        } catch (Exception e) {
            throw new RuntimeException("预览失败: " + e.getMessage(), e);
        }
    }

    @PostMapping("/doc/analyze")
    public Map<String, Object> analyzeDoc(
            @RequestParam("user_id") Long userId,
            @RequestParam("subject_id") Long subjectId,
            @RequestParam("file") MultipartFile file) {
        Subject subject = subjectRepository.findById(subjectId)
                .orElseThrow(() -> new IllegalArgumentException("科目不存在"));
        if (!subject.getUserId().equals(userId)) {
            throw new IllegalArgumentException("无权访问此科目");
        }

        String name = file.getOriginalFilename() == null ? "" : file.getOriginalFilename().toLowerCase();
        boolean isDocx = name.endsWith(".docx");
        boolean isDoc = name.endsWith(".doc");
        if (!isDocx && !isDoc) {
            throw new IllegalArgumentException("当前分析功能仅支持 .docx / .doc");
        }

        Path tempFile = null;
        try {
            tempFile = Files.createTempFile("analyze_", "_" + file.getOriginalFilename());
            Files.copy(file.getInputStream(), tempFile, StandardCopyOption.REPLACE_EXISTING);

            Map<String, Object> parsedData = aiParserService.parseFileToQuestions(userId, tempFile, subject.getName());
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> questions = (List<Map<String, Object>>) parsedData.get("questions");
            if (questions == null) {
                questions = List.of();
            }
            for (int i = 0; i < questions.size(); i++) {
                questions.get(i).put("index", i + 1);
            }

            DocxImageExtractService.DocxExtractResult extractResult = isDocx
                    ? docxImageExtractService.extract(file.getBytes())
                    : docxImageExtractService.extractDoc(file.getInputStream());

            return Map.of(
                    "success", true,
                    "file_type", isDocx ? "docx" : "doc",
                    "questions", questions,
                    "images", extractResult.getImages(),
                    "question_numbers", extractResult.getQuestionNumbers(),
                    "message", "分析完成：识别题目 " + questions.size() + " 道，提取图片 " + extractResult.getImages().size() + " 张"
            );
        } catch (Exception e) {
            throw new RuntimeException("分析失败: " + e.getMessage(), e);
        } finally {
            if (tempFile != null) {
                try {
                    Files.deleteIfExists(tempFile);
                } catch (Exception ignored) {
                }
            }
        }
    }

    @PostMapping("/doc/bind-images")
    public Map<String, Object> bindDocImages(@RequestBody BindDocImagesRequest request) {
        if (request == null || request.getUserId() == null || request.getSubjectId() == null) {
            throw new IllegalArgumentException("参数不完整");
        }
        Subject subject = subjectRepository.findById(request.getSubjectId())
                .orElseThrow(() -> new IllegalArgumentException("科目不存在"));
        if (!subject.getUserId().equals(request.getUserId())) {
            throw new IllegalArgumentException("无权访问此科目");
        }

        List<Question> questions = questionRepository.findBySubjectId(request.getSubjectId());
        if (questions == null || questions.isEmpty()) {
            throw new IllegalArgumentException("该题目集暂无题目，请先完成题目导入");
        }

        int bound = 0;
        int skipped = 0;

        List<DocumentImageItem> images = request.getImages();
        if (images == null || images.isEmpty()) {
            return Map.of("success", true, "message", "未发现需要绑定的图片", "bound", 0, "skipped", 0);
        }

        for (DocumentImageItem img : images) {
            if (img == null || img.getGuessedQuestionNo() == null) {
                skipped++;
                continue;
            }

            Integer qno = img.getGuessedQuestionNo();
            Optional<Question> matched = findQuestionByNo(questions, qno);
            if (matched.isEmpty()) {
                skipped++;
                continue;
            }

            byte[] bytes;
            try {
                bytes = Base64.getDecoder().decode(img.getBase64());
            } catch (Exception e) {
                skipped++;
                continue;
            }

            resourceService.saveImageBytes(matched.get().getId(), img.getIndex(), bytes, img.getFileName());
            bound++;
        }

        return Map.of(
                "success", true,
                "message", "已绑定 " + bound + " 张图片（跳过 " + skipped + " 张）",
                "bound", bound,
                "skipped", skipped
        );
    }

    @PostMapping("/doc/confirm")
    public Map<String, Object> confirmDocAnalyze(@RequestBody DocAnalyzeConfirmRequest request) {
        if (request == null || request.getUserId() == null || request.getSubjectId() == null) {
            throw new IllegalArgumentException("参数不完整");
        }
        Subject subject = subjectRepository.findById(request.getSubjectId())
                .orElseThrow(() -> new IllegalArgumentException("科目不存在"));
        if (!subject.getUserId().equals(request.getUserId())) {
            throw new IllegalArgumentException("无权访问此科目");
        }

        List<Map<String, Object>> questionsData = request.getQuestions();
        if (questionsData == null || questionsData.isEmpty()) {
            throw new IllegalArgumentException("没有可导入的题目");
        }

        Map<String, Object> importResult = questionService.batchCreateQuestions(
                request.getUserId(), request.getSubjectId(), questionsData, true);
        int createdCount = (Integer) importResult.getOrDefault("created_count", 0);
        int skippedDup = (Integer) importResult.getOrDefault("skipped_count", 0);

        Map<Integer, Long> indexToQuestionId = new HashMap<>();
        for (int i = 0; i < questionsData.size(); i++) {
            Map<String, Object> qMap = questionsData.get(i);
            String qText = String.valueOf(qMap.getOrDefault("question", ""));
            if (qText == null || qText.isBlank()) continue;
            List<Question> matched = questionRepository.findByUserIdAndSubjectIdAndQuestion(
                    request.getUserId(), request.getSubjectId(), qText);
            if (matched == null || matched.isEmpty()) continue;
            Question latest = matched.stream().max(Comparator.comparing(Question::getId)).orElse(null);
            if (latest != null) {
                indexToQuestionId.put(i + 1, latest.getId());
            }
        }

        int bound = 0;
        int skippedBind = 0;
        List<DocumentImageItem> images = request.getImages();
        if (images != null) {
            for (DocumentImageItem img : images) {
                if (img == null || img.getMappedQuestionIndex() == null) {
                    skippedBind++;
                    continue;
                }
                Long questionId = indexToQuestionId.get(img.getMappedQuestionIndex());
                if (questionId == null) {
                    skippedBind++;
                    continue;
                }
                try {
                    byte[] bytes = Base64.getDecoder().decode(img.getBase64());
                    resourceService.saveImageBytes(questionId, img.getIndex(), bytes, img.getFileName());
                    bound++;
                } catch (Exception e) {
                    skippedBind++;
                }
            }
        }

        return Map.of(
                "success", true,
                "message", "已导入题目 " + createdCount + " 道（重复跳过 " + skippedDup + " 道），绑定图片 " + bound + " 张（跳过 " + skippedBind + " 张）",
                "created_count", createdCount,
                "skipped_duplicate_count", skippedDup,
                "bound_image_count", bound,
                "skipped_image_count", skippedBind
        );
    }

    private Optional<Question> findQuestionByNo(List<Question> questions, Integer qno) {
        if (qno == null) return Optional.empty();
        String n = String.valueOf(qno);
        Pattern p1 = Pattern.compile("^\\s*" + Pattern.quote(n) + "\\s*[\\.、]\\s*");
        Pattern p2 = Pattern.compile("^\\s*[（(]\\s*" + Pattern.quote(n) + "\\s*[）)]\\s*");
        for (Question q : questions) {
            if (q == null || q.getQuestion() == null) continue;
            String t = q.getQuestion().trim();
            if (p1.matcher(t).find() || p2.matcher(t).find()) {
                return Optional.of(q);
            }
        }
        return Optional.empty();
    }

    @PostMapping("/file")
    public ImportResponse importFromFile(
            @RequestParam("user_id") Long userId,
            @RequestParam("subject_id") Long subjectId,
            @RequestParam("file") MultipartFile file) {
        Subject subject = subjectRepository.findById(subjectId)
                .orElseThrow(() -> new IllegalArgumentException("科目不存在"));
        if (!subject.getUserId().equals(userId)) {
            throw new IllegalArgumentException("无权访问此科目");
        }

        Path tempFile = null;
        try {
            tempFile = Files.createTempFile("import_", "_" + file.getOriginalFilename());
            Files.copy(file.getInputStream(), tempFile, StandardCopyOption.REPLACE_EXISTING);

            Map<String, Object> parsedData = aiParserService.parseFileToQuestions(userId, tempFile, subject.getName());

            @SuppressWarnings("unchecked")
            List<Map<String, Object>> questions = (List<Map<String, Object>>) parsedData.get("questions");
            if (questions == null || questions.isEmpty()) {
                throw new IllegalArgumentException("AI未能从文件中解析出任何题目，请检查文件内容是否为清晰的试题文本或适当拆分后重试");
            }

            Map<String, Object> result = questionService.batchCreateQuestions(userId, subjectId, questions, true);

            int createdCount = (Integer) result.get("created_count");
            int skippedCount = (Integer) result.get("skipped_count");

            String message = "成功导入 " + createdCount + " 道题目";
            if (skippedCount > 0) {
                message += "（跳过 " + skippedCount + " 道重复题目）";
            }

            return new ImportResponse(true, message, createdCount);
        } catch (Exception e) {
            throw new RuntimeException("导入失败: " + e.getMessage(), e);
        } finally {
            if (tempFile != null) {
                try {
                    Files.deleteIfExists(tempFile);
                } catch (Exception ignored) {
                }
            }
        }
    }

    @PostMapping("/image")
    public ImportResponse importFromImage(
            @RequestParam("user_id") Long userId,
            @RequestParam("subject_id") Long subjectId,
            @RequestParam("image") MultipartFile image) {
        Subject subject = subjectRepository.findById(subjectId)
                .orElseThrow(() -> new IllegalArgumentException("科目不存在"));
        if (!subject.getUserId().equals(userId)) {
            throw new IllegalArgumentException("无权访问此科目");
        }

        Path tempFile = null;
        try {
            tempFile = Files.createTempFile("import_image_", "_" + image.getOriginalFilename());
            Files.copy(image.getInputStream(), tempFile, StandardCopyOption.REPLACE_EXISTING);

            Map<String, Object> parsedData = aiParserService.parseImageToQuestions(userId, tempFile, subject.getName());

            @SuppressWarnings("unchecked")
            List<Map<String, Object>> questions = (List<Map<String, Object>>) parsedData.get("questions");
            if (questions == null || questions.isEmpty()) {
                throw new IllegalArgumentException("AI未能从图片中识别出题目，请确保图片清晰且包含完整题目");
            }

            Map<String, Object> result = questionService.batchCreateQuestions(userId, subjectId, questions, true);

            int createdCount = (Integer) result.get("created_count");
            int skippedCount = (Integer) result.get("skipped_count");

            String message = "成功导入 " + createdCount + " 道题目";
            if (skippedCount > 0) {
                message += "（跳过 " + skippedCount + " 道重复题目）";
            }

            return new ImportResponse(true, message, createdCount);
        } catch (Exception e) {
            throw new RuntimeException("导入失败: " + e.getMessage(), e);
        } finally {
            if (tempFile != null) {
                try {
                    Files.deleteIfExists(tempFile);
                } catch (Exception ignored) {
                }
            }
        }
    }

    @PostMapping("/text")
    public ImportResponse importFromText(@RequestBody TextImportRequest request) {
        Subject subject = subjectRepository.findById(request.getSubjectId())
                .orElseThrow(() -> new IllegalArgumentException("科目不存在"));
        if (!subject.getUserId().equals(request.getUserId())) {
            throw new IllegalArgumentException("无权访问此科目");
        }

        try {
            Map<String, Object> parsedData = aiParserService.parseTextToQuestions(
                    request.getUserId(), request.getText(), subject.getName());

            @SuppressWarnings("unchecked")
            List<Map<String, Object>> questions = (List<Map<String, Object>>) parsedData.get("questions");
            if (questions == null || questions.isEmpty()) {
                throw new IllegalArgumentException("AI未能从文本中解析出任何题目，请确认文本包含清晰的题号、选项和答案信息");
            }

            Map<String, Object> result = questionService.batchCreateQuestions(
                    request.getUserId(), request.getSubjectId(), questions, true);

            int createdCount = (Integer) result.get("created_count");
            int skippedCount = (Integer) result.get("skipped_count");

            String message = "成功导入 " + createdCount + " 道题目";
            if (skippedCount > 0) {
                message += "（跳过 " + skippedCount + " 道重复题目）";
            }

            return new ImportResponse(true, message, createdCount);
        } catch (Exception e) {
            throw new RuntimeException("导入失败: " + e.getMessage(), e);
        }
    }
}
