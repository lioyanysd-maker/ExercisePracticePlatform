package com.lio.exercisepracticesystem.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.lio.exercisepracticesystem.dto.*;
import com.lio.exercisepracticesystem.entity.ErrorBook;
import com.lio.exercisepracticesystem.entity.PracticeRecord;
import com.lio.exercisepracticesystem.entity.PracticeSession;
import com.lio.exercisepracticesystem.entity.Question;
import com.lio.exercisepracticesystem.entity.Subject;
import com.lio.exercisepracticesystem.repository.ErrorBookRepository;
import com.lio.exercisepracticesystem.repository.PracticeRecordRepository;
import com.lio.exercisepracticesystem.repository.PracticeSessionRepository;
import com.lio.exercisepracticesystem.repository.QuestionRepository;
import com.lio.exercisepracticesystem.repository.SubjectRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class PracticeService {

    private final QuestionService questionService;
    private final PracticeSessionRepository practiceSessionRepository;
    private final PracticeRecordRepository practiceRecordRepository;
    private final ErrorBookRepository errorBookRepository;
    private final ObjectMapper objectMapper;
    private final QuestionRepository questionRepository;
    private final SubjectRepository subjectRepository;
    private final ShareService shareService;

    public PracticeService(QuestionService questionService,
                           PracticeSessionRepository practiceSessionRepository,
                           PracticeRecordRepository practiceRecordRepository,
                           ErrorBookRepository errorBookRepository,
                           ObjectMapper objectMapper,
                           QuestionRepository questionRepository,
                           SubjectRepository subjectRepository,
                           ShareService shareService) {
        this.questionService = questionService;
        this.practiceSessionRepository = practiceSessionRepository;
        this.practiceRecordRepository = practiceRecordRepository;
        this.errorBookRepository = errorBookRepository;
        this.objectMapper = objectMapper;
        this.questionRepository = questionRepository;
        this.subjectRepository = subjectRepository;
        this.shareService = shareService;
    }

    public PracticeResponse startPractice(PracticeRequest request) {
        if (!shareService.canAccessSubject(request.getUserId(), request.getSubjectId())) {
            throw new IllegalArgumentException("无权访问此科目");
        }
        List<Question> questions = questionService.randomSelect(
                request.getUserId(),
                request.getSubjectId(),
                request.getQuestionCounts()
        );
        if (questions.isEmpty()) {
            return new PracticeResponse(Collections.emptyList());
        }
        List<QuestionItem> items = questions.stream().map(q -> {
            List<String> options = parseOptions(q.getOptionsJson());
            return new QuestionItem(q.getId(), q.getType().name(), q.getQuestion(), options);
        }).collect(Collectors.toList());
        return new PracticeResponse(items);
    }

    @Transactional
    public SubmitAnswersResponse submitAnswers(SubmitAnswersRequest request) {
        int correct = 0;
        int wrong = 0;
        List<Map<String, Object>> resultList = new ArrayList<>();
        List<Long> errorQuestionIds = new ArrayList<>();

        for (AnswerItem item : request.getAnswers()) {
            Question q = findQuestion(item.getQuestionId());
            if (q == null) continue;

            boolean isCorrect = checkAnswer(q, item.getUserAnswer());
            if (isCorrect) {
                correct++;
            } else {
                wrong++;
                errorQuestionIds.add(q.getId());
            }

            Map<String, Object> m = new LinkedHashMap<>();
            m.put("question_id", q.getId());
            m.put("type", q.getType().name());
            m.put("question", q.getQuestion());
            m.put("options", parseOptions(q.getOptionsJson()));
            m.put("user_answer", item.getUserAnswer());
            m.put("correct_answer", q.getAnswer());
            m.put("is_correct", isCorrect);
            m.put("analysis", q.getAnalysis());
            resultList.add(m);
        }

        int total = request.getAnswers() != null ? request.getAnswers().size() : 0;
        double accuracy = total > 0 ? (correct * 100.0 / total) : 0.0;
        String grade = calcGrade(accuracy);

        PracticeSession session = new PracticeSession();
        session.setUserId(request.getUserId());
        session.setSubjectId(request.getSubjectId());
        session.setTotalCount(total);
        session.setCorrectCount(correct);
        session.setWrongCount(wrong);
        session.setAccuracy(String.format(Locale.ROOT, "%.2f", accuracy));
        session.setGrade(grade);
        session.setCreatedAt(LocalDateTime.now());
        PracticeSession savedSession = practiceSessionRepository.save(session);

        for (AnswerItem item : request.getAnswers()) {
            Question q = findQuestion(item.getQuestionId());
            if (q == null) continue;
            boolean isCorrect = checkAnswer(q, item.getUserAnswer());

            PracticeRecord record = new PracticeRecord();
            record.setSessionId(savedSession.getId());
            record.setUserId(request.getUserId());
            record.setSubjectId(request.getSubjectId());
            record.setQuestionId(q.getId());
            record.setUserAnswer(item.getUserAnswer());
            record.setIsCorrect(isCorrect ? 1 : 0);
            record.setCreatedAt(LocalDateTime.now());
            practiceRecordRepository.save(record);
        }

        if (!errorQuestionIds.isEmpty()) {
            batchAddErrors(request.getUserId(), request.getSubjectId(), errorQuestionIds);
        }

        return new SubmitAnswersResponse(total, correct, wrong,
                Math.round(accuracy * 100.0) / 100.0, grade, resultList);
    }

    private Question findQuestion(Long id) {
        return questionRepository.findById(id).orElse(null);
    }

    private boolean checkAnswer(Question q, String userAnswer) {
        if (userAnswer == null) return false;
        String ua = userAnswer.trim();
        String ca = q.getAnswer() != null ? q.getAnswer().trim() : "";
        return ua.equalsIgnoreCase(ca);
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

    private String calcGrade(double accuracy) {
        if (accuracy >= 90) return "A";
        if (accuracy >= 80) return "B";
        if (accuracy >= 70) return "C";
        if (accuracy >= 60) return "D";
        return "F";
    }

    @Transactional
    protected void batchAddErrors(Long userId, Long subjectId, List<Long> questionIds) {
        java.time.OffsetDateTime now = java.time.OffsetDateTime.now();
        for (Long qid : questionIds) {
            ErrorBook error = errorBookRepository
                    .findByUserIdAndSubjectIdAndQuestionId(userId, subjectId, qid)
                    .orElseGet(() -> {
                        ErrorBook e = new ErrorBook();
                        e.setUserId(userId);
                        e.setSubjectId(subjectId);
                        e.setQuestionId(qid);
                        e.setWrongCount(0);
                        return e;
                    });
            error.setWrongCount((error.getWrongCount() == null ? 0 : error.getWrongCount()) + 1);
            error.setLastWrongAt(now);
            errorBookRepository.save(error);
        }
    }

    public StatisticsResponse getTodayStatistics(Long userId) {
        LocalDateTime today = LocalDateTime.now().withHour(0).withMinute(0).withSecond(0).withNano(0);
        return getStatistics(userId, today);
    }

    public StatisticsResponse getWeekStatistics(Long userId) {
        LocalDateTime weekAgo = LocalDateTime.now().minusDays(7);
        return getStatistics(userId, weekAgo);
    }

    public StatisticsResponse getAllStatistics(Long userId) {
        return getStatistics(userId, null);
    }

    private StatisticsResponse getStatistics(Long userId, LocalDateTime after) {
        long totalCount;
        long correctCount;

        if (after != null) {
            totalCount = practiceRecordRepository.countByUserIdAndCreatedAtAfter(userId, after);
            correctCount = practiceRecordRepository.countCorrectByUserIdAndCreatedAtAfter(userId, after);
        } else {
            totalCount = practiceRecordRepository.countByUserId(userId);
            correctCount = practiceRecordRepository.countCorrectByUserId(userId);
        }

        int wrongCount = (int) (totalCount - correctCount);
        double accuracy = totalCount > 0 ? (correctCount * 100.0 / totalCount) : 0.0;
        String grade = calcGrade(accuracy);

        return new StatisticsResponse(
                (int) totalCount,
                (int) correctCount,
                wrongCount,
                Math.round(accuracy * 100.0) / 100.0,
                grade
        );
    }

    public int getConsecutiveDays(Long userId) {
        List<PracticeRecord> records = practiceRecordRepository.findByUserId(userId);
        if (records.isEmpty()) {
            return 0;
        }

        Set<java.time.LocalDate> practiceDates = records.stream()
                .map(r -> r.getCreatedAt() != null ? r.getCreatedAt().toLocalDate() : null)
                .filter(Objects::nonNull)
                .sorted(Collections.reverseOrder())
                .collect(Collectors.toCollection(LinkedHashSet::new));

        if (practiceDates.isEmpty()) {
            return 0;
        }

        java.time.LocalDate today = java.time.LocalDate.now();
        if (!practiceDates.contains(today)) {
            return 0;
        }

        int consecutive = 1;
        java.time.LocalDate currentDate = today;
        while (practiceDates.contains(currentDate.minusDays(consecutive))) {
            consecutive++;
        }

        return consecutive;
    }

    public HomeStatisticsResponse getHomeStatistics(Long userId) {
        StatisticsResponse today = getTodayStatistics(userId);
        StatisticsResponse week = getWeekStatistics(userId);
        StatisticsResponse all = getAllStatistics(userId);
        int consecutiveDays = getConsecutiveDays(userId);

        return new HomeStatisticsResponse(today, week, all, consecutiveDays);
    }

    public List<PracticeHistoryItem> getPracticeHistory(Long userId, Long subjectId, Integer limit) {
        List<PracticeSession> sessions;
        if (subjectId != null) {
            sessions = practiceSessionRepository.findByUserIdAndSubjectIdOrderByCreatedAtDesc(userId, subjectId);
        } else {
            if (limit != null && limit > 0) {
                Pageable pageable = PageRequest.of(0, limit);
                sessions = practiceSessionRepository.findByUserIdWithLimit(userId, pageable);
            } else {
                sessions = practiceSessionRepository.findByUserIdOrderByCreatedAtDesc(userId);
            }
        }

        if (limit != null && limit > 0 && sessions.size() > limit) {
            sessions = sessions.subList(0, limit);
        }

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        return sessions.stream().map(session -> {
            Subject subject = subjectRepository.findById(session.getSubjectId()).orElse(null);
            String subjectName = subject != null ? subject.getName() : "未知科目";
            String practiceDate = session.getCreatedAt() != null
                    ? session.getCreatedAt().format(formatter)
                    : null;
            double accuracy = session.getAccuracy() != null
                    ? Double.parseDouble(session.getAccuracy())
                    : 0.0;

            return new PracticeHistoryItem(
                    session.getId(),
                    practiceDate,
                    session.getTotalCount(),
                    session.getCorrectCount(),
                    session.getWrongCount(),
                    accuracy,
                    session.getGrade(),
                    subjectName
            );
        }).collect(Collectors.toList());
    }

    public Map<String, List<Map<String, Object>>> getSessionDetails(Long sessionId, Long userId) {
        PracticeSession session = practiceSessionRepository.findById(sessionId)
                .orElseThrow(() -> new IllegalArgumentException("练习记录不存在"));
        if (!session.getUserId().equals(userId)) {
            throw new IllegalArgumentException("无权查看此练习记录");
        }
        List<PracticeRecord> records = practiceRecordRepository.findBySessionIdOrderById(sessionId);
        List<Map<String, Object>> details = new ArrayList<>();
        for (PracticeRecord rec : records) {
            Question q = questionRepository.findById(rec.getQuestionId()).orElse(null);
            if (q == null) continue;
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("type", q.getType().name());
            item.put("question", q.getQuestion());
            item.put("options", parseOptions(q.getOptionsJson()));
            item.put("correct_answer", q.getAnswer());
            item.put("user_answer", rec.getUserAnswer() != null ? rec.getUserAnswer() : "");
            item.put("is_correct", rec.getIsCorrect() != null && rec.getIsCorrect() == 1);
            item.put("analysis", q.getAnalysis() != null ? q.getAnalysis() : "");
            details.add(item);
        }
        return Map.of("details", details);
    }
}

