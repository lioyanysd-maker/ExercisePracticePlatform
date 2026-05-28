package com.lio.exercisepracticesystem.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.lio.exercisepracticesystem.dto.QuestionCreate;
import com.lio.exercisepracticesystem.dto.QuestionResponse;
import com.lio.exercisepracticesystem.dto.QuestionUpdate;
import com.lio.exercisepracticesystem.entity.Question;
import com.lio.exercisepracticesystem.entity.QuestionType;
import com.lio.exercisepracticesystem.repository.QuestionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class QuestionService {

    private final QuestionRepository questionRepository;
    private final ObjectMapper objectMapper;

    public QuestionService(QuestionRepository questionRepository, ObjectMapper objectMapper) {
        this.questionRepository = questionRepository;
        this.objectMapper = objectMapper;
    }

    @Transactional
    public QuestionResponse create(QuestionCreate request) {
        Question question = new Question();
        question.setUserId(request.getUserId());
        question.setSubjectId(request.getSubjectId());
        question.setType(QuestionType.valueOf(request.getType()));
        question.setQuestion(request.getQuestion());
        question.setAnswer(request.getAnswer());
        question.setAnalysis(request.getAnalysis());
        try {
            question.setOptionsJson(objectMapper.writeValueAsString(request.getOptions()));
        } catch (JsonProcessingException e) {
            throw new IllegalArgumentException("选项格式不正确");
        }
        question.setCreatedAt(java.time.LocalDateTime.now());

        Question saved = questionRepository.save(question);
        return toResponse(saved);
    }

    public List<QuestionResponse> list(Long userId, Long subjectId, String type, Integer limit) {
        List<Question> list;
        if (type != null && !type.isEmpty()) {
            list = questionRepository.findBySubjectIdAndType(subjectId, QuestionType.valueOf(type));
        } else {
            list = questionRepository.findBySubjectId(subjectId);
        }
        if (limit != null && limit > 0 && list.size() > limit) {
            list = list.subList(0, limit);
        }
        return list.stream().map(this::toResponse).collect(Collectors.toList());
    }

    public List<Question> randomSelect(Long userId, Long subjectId, Map<String, Integer> questionCounts) {
        List<Question> result = new ArrayList<>();
        Random random = new Random();

        for (Map.Entry<String, Integer> entry : questionCounts.entrySet()) {
            String type = entry.getKey();
            int count = entry.getValue() != null ? entry.getValue() : 0;
            if (count <= 0) {
                continue;
            }
            QuestionType qt = QuestionType.valueOf(type);
            List<Question> pool = questionRepository.findBySubjectIdAndType(subjectId, qt);
            if (pool.isEmpty()) {
                continue;
            }
            Collections.shuffle(pool, random);
            result.addAll(pool.subList(0, Math.min(count, pool.size())));
        }
        return result;
    }

    public QuestionResponse get(Long questionId, Long userId) {
        Question q = questionRepository.findById(questionId)
                .orElseThrow(() -> new IllegalArgumentException("题目不存在"));
        return toResponse(q);
    }

    public List<String> getQuestionTypesBySubject(Long subjectId) {
        List<Question> questions = questionRepository.findBySubjectId(subjectId);
        return questions.stream()
                .map(q -> q.getType().name())
                .distinct()
                .sorted()
                .collect(Collectors.toList());
    }

    @Transactional
    public QuestionResponse update(Long questionId, Long userId, QuestionUpdate request) {
        Question q = questionRepository.findById(questionId)
                .orElseThrow(() -> new IllegalArgumentException("题目不存在"));
        if (!q.getUserId().equals(userId)) {
            throw new IllegalArgumentException("无权修改此题目");
        }
        if (request.getType() != null && !request.getType().isBlank()) {
            q.setType(QuestionType.valueOf(request.getType()));
        }
        if (request.getQuestion() != null) {
            q.setQuestion(request.getQuestion());
        }
        if (request.getAnswer() != null) {
            q.setAnswer(request.getAnswer());
        }
        if (request.getAnalysis() != null) {
            q.setAnalysis(request.getAnalysis());
        }
        if (request.getOptions() != null) {
            try {
                q.setOptionsJson(objectMapper.writeValueAsString(request.getOptions()));
            } catch (JsonProcessingException e) {
                throw new IllegalArgumentException("选项格式不正确");
            }
        }
        Question saved = questionRepository.save(q);
        return toResponse(saved);
    }

    @Transactional
    public void delete(Long questionId, Long userId) {
        Question q = questionRepository.findById(questionId)
                .orElseThrow(() -> new IllegalArgumentException("题目不存在"));
        if (!q.getUserId().equals(userId)) {
            throw new IllegalArgumentException("无权删除此题目");
        }
        questionRepository.delete(q);
    }

    public boolean checkDuplicate(Long userId, Long subjectId, String questionText, String questionType) {
        List<Question> existing = questionRepository.findByUserIdAndSubjectId(userId, subjectId);
        return existing.stream().anyMatch(q ->
                q.getQuestion().equals(questionText) && q.getType().name().equals(questionType)
        );
    }

    @Transactional
    public Map<String, Object> batchCreateQuestions(Long userId, Long subjectId,
                                                     List<Map<String, Object>> questionsData,
                                                     boolean skipDuplicates) {
        int createdCount = 0;
        int skippedCount = 0;

        for (Map<String, Object> qData : questionsData) {
            String questionText = (String) qData.get("question");
            String questionType = (String) qData.get("type");

            if (skipDuplicates && checkDuplicate(userId, subjectId, questionText, questionType)) {
                skippedCount++;
                continue;
            }

            Question question = new Question();
            question.setUserId(userId);
            question.setSubjectId(subjectId);
            try {
                question.setType(QuestionType.valueOf(questionType.toLowerCase()));
            } catch (IllegalArgumentException e) {
                throw new IllegalArgumentException("无效的题目类型: " + questionType);
            }
            question.setQuestion(questionText);
            question.setAnswer((String) qData.get("answer"));
            question.setAnalysis((String) qData.getOrDefault("analysis", ""));

            @SuppressWarnings("unchecked")
            List<String> options = (List<String>) qData.getOrDefault("options", Collections.emptyList());
            try {
                question.setOptionsJson(objectMapper.writeValueAsString(options));
            } catch (JsonProcessingException e) {
                question.setOptionsJson("[]");
            }
            question.setCreatedAt(java.time.LocalDateTime.now());

            questionRepository.save(question);
            createdCount++;
        }

        Map<String, Object> result = new HashMap<>();
        result.put("created_count", createdCount);
        result.put("skipped_count", skippedCount);
        return result;
    }

    private QuestionResponse toResponse(Question q) {
        List<String> options;
        if (q.getOptionsJson() != null) {
            try {
                options = objectMapper.readValue(q.getOptionsJson(), objectMapper.getTypeFactory()
                        .constructCollectionType(List.class, String.class));
            } catch (Exception e) {
                options = Collections.emptyList();
            }
        } else {
            options = Collections.emptyList();
        }

        String createdAt = q.getCreatedAt() != null
                ? q.getCreatedAt().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))
                : null;

        return new QuestionResponse(
                q.getId(),
                q.getSubjectId(),
                q.getUserId(),
                q.getType().name(),
                q.getQuestion(),
                options,
                q.getAnswer(),
                q.getAnalysis(),
                createdAt
        );
    }
}

