package com.lio.exercisepracticesystem.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.lio.exercisepracticesystem.dto.ErrorQuestionResponse;
import com.lio.exercisepracticesystem.dto.PracticeRequest;
import com.lio.exercisepracticesystem.dto.PracticeResponse;
import com.lio.exercisepracticesystem.dto.QuestionItem;
import com.lio.exercisepracticesystem.entity.ErrorBook;
import com.lio.exercisepracticesystem.entity.Question;
import com.lio.exercisepracticesystem.entity.QuestionType;
import com.lio.exercisepracticesystem.repository.ErrorBookRepository;
import com.lio.exercisepracticesystem.repository.QuestionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class ErrorService {

    private final ErrorBookRepository errorBookRepository;
    private final QuestionRepository questionRepository;
    private final ObjectMapper objectMapper;

    public ErrorService(ErrorBookRepository errorBookRepository,
                        QuestionRepository questionRepository,
                        ObjectMapper objectMapper) {
        this.errorBookRepository = errorBookRepository;
        this.questionRepository = questionRepository;
        this.objectMapper = objectMapper;
    }

    public List<ErrorQuestionResponse> getErrorQuestions(Long userId, Long subjectId, Integer limit) {
        List<ErrorBook> errors;
        if (subjectId != null) {
            errors = errorBookRepository.findByUserIdAndSubjectIdOrderByLastWrongAtDesc(userId, subjectId);
        } else {
            errors = errorBookRepository.findByUserIdOrderByLastWrongAtDesc(userId);
        }

        if (limit != null && limit > 0 && errors.size() > limit) {
            errors = errors.subList(0, limit);
        }

        return errors.stream().map(error -> {
            Question q = questionRepository.findById(error.getQuestionId()).orElse(null);
            if (q == null) return null;

            List<String> options = parseOptions(q.getOptionsJson());
            String lastWrongAt = error.getLastWrongAt() != null
                    ? error.getLastWrongAt().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))
                    : null;

            return new ErrorQuestionResponse(
                    error.getId(),
                    q.getId(),
                    q.getSubjectId(),
                    q.getType().name(),
                    q.getQuestion(),
                    options,
                    q.getAnswer(),
                    q.getAnalysis(),
                    error.getWrongCount(),
                    lastWrongAt
            );
        }).filter(Objects::nonNull).collect(Collectors.toList());
    }

    public long getErrorCount(Long userId, Long subjectId) {
        if (subjectId != null) {
            return errorBookRepository.countByUserIdAndSubjectId(userId, subjectId);
        }
        return errorBookRepository.countByUserId(userId);
    }

    public List<String> getErrorTypesBySubject(Long userId, Long subjectId) {
        List<QuestionType> types = errorBookRepository.findDistinctQuestionTypesByUserIdAndSubjectId(userId, subjectId);
        return types.stream()
                .map(QuestionType::name)
                .collect(Collectors.toList());
    }

    public PracticeResponse startErrorPractice(PracticeRequest request) {
        List<Question> questions = new ArrayList<>();
        Random random = new Random();

        for (Map.Entry<String, Integer> entry : request.getQuestionCounts().entrySet()) {
            String type = entry.getKey();
            int count = entry.getValue() != null ? entry.getValue() : 0;
            if (count <= 0) continue;

            QuestionType qt = QuestionType.valueOf(type);
            List<ErrorBook> errorBooks = errorBookRepository.findByUserIdAndSubjectIdOrderByLastWrongAtDesc(
                    request.getUserId(), request.getSubjectId());

            List<Question> typeQuestions = errorBooks.stream()
                    .map(eb -> questionRepository.findById(eb.getQuestionId()).orElse(null))
                    .filter(Objects::nonNull)
                    .filter(q -> q.getType() == qt)
                    .collect(Collectors.toList());

            Collections.shuffle(typeQuestions, random);
            questions.addAll(typeQuestions.subList(0, Math.min(count, typeQuestions.size())));
        }

        List<QuestionItem> items = questions.stream().map(q -> {
            List<String> options = parseOptions(q.getOptionsJson());
            return new QuestionItem(q.getId(), q.getType().name(), q.getQuestion(), options);
        }).collect(Collectors.toList());

        return new PracticeResponse(items);
    }

    @Transactional
    public boolean removeError(Long errorId, Long userId) {
        Optional<ErrorBook> errorOpt = errorBookRepository.findById(errorId);
        if (errorOpt.isEmpty()) {
            return false;
        }
        ErrorBook error = errorOpt.get();
        if (!error.getUserId().equals(userId)) {
            return false;
        }
        errorBookRepository.delete(error);
        return true;
    }

    private List<String> parseOptions(String json) {
        if (json == null) return Collections.emptyList();
        try {
            return objectMapper.readValue(json,
                    objectMapper.getTypeFactory().constructCollectionType(List.class, String.class));
        } catch (Exception e) {
            return Collections.emptyList();
        }
    }
}
