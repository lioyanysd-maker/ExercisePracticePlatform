package com.lio.exercisepracticesystem.service;

import com.lio.exercisepracticesystem.dto.admin.ActivityItemDto;
import com.lio.exercisepracticesystem.dto.admin.AdminDashboardSummaryDto;
import com.lio.exercisepracticesystem.dto.admin.AdminOverviewDto;
import com.lio.exercisepracticesystem.dto.admin.PracticeTrendDayDto;
import com.lio.exercisepracticesystem.entity.PracticeSession;
import com.lio.exercisepracticesystem.entity.User;
import com.lio.exercisepracticesystem.repository.ErrorBookRepository;
import com.lio.exercisepracticesystem.repository.PracticeSessionRepository;
import com.lio.exercisepracticesystem.repository.QuestionRepository;
import com.lio.exercisepracticesystem.repository.SubjectRepository;
import com.lio.exercisepracticesystem.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.sql.Date;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class AdminDashboardService {

    private static final DateTimeFormatter TREND_LABEL = DateTimeFormatter.ofPattern("MM-dd");

    private final UserRepository userRepository;
    private final SubjectRepository subjectRepository;
    private final QuestionRepository questionRepository;
    private final ErrorBookRepository errorBookRepository;
    private final PracticeSessionRepository practiceSessionRepository;

    public AdminDashboardService(
            UserRepository userRepository,
            SubjectRepository subjectRepository,
            QuestionRepository questionRepository,
            ErrorBookRepository errorBookRepository,
            PracticeSessionRepository practiceSessionRepository) {
        this.userRepository = userRepository;
        this.subjectRepository = subjectRepository;
        this.questionRepository = questionRepository;
        this.errorBookRepository = errorBookRepository;
        this.practiceSessionRepository = practiceSessionRepository;
    }

    public AdminDashboardSummaryDto buildSummary(int trendDays) {
        int days = Math.max(3, Math.min(trendDays, 30));
        AdminOverviewDto overview = buildOverview();
        List<PracticeTrendDayDto> trend = buildTrend(days);
        List<ActivityItemDto> activity = buildActivity();
        return new AdminDashboardSummaryDto(overview, trend, activity);
    }

    private AdminOverviewDto buildOverview() {
        long users = userRepository.count();
        long subjects = subjectRepository.count();
        long questions = questionRepository.count();
        long errors = errorBookRepository.count();
        long todaySessions = practiceSessionRepository.countTodaySessions();
        long todayActive = practiceSessionRepository.countTodayActiveUsers();
        return new AdminOverviewDto(users, subjects, questions, errors, todaySessions, todayActive);
    }

    private List<PracticeTrendDayDto> buildTrend(int days) {
        LocalDate end = LocalDate.now();
        LocalDate start = end.minusDays(days - 1);
        LocalDateTime from = start.atStartOfDay();

        List<Object[]> rows = practiceSessionRepository.countPracticeGroupedByDaySince(from);
        Map<LocalDate, Long> counts = new HashMap<>();
        for (Object[] row : rows) {
            LocalDate d;
            if (row[0] instanceof Date) {
                d = ((Date) row[0]).toLocalDate();
            } else if (row[0] instanceof java.time.LocalDate) {
                d = (LocalDate) row[0];
            } else {
                continue;
            }
            long n = row[1] instanceof Number ? ((Number) row[1]).longValue() : 0L;
            counts.put(d, n);
        }

        List<PracticeTrendDayDto> list = new ArrayList<>();
        for (int i = 0; i < days; i++) {
            LocalDate d = start.plusDays(i);
            long c = counts.getOrDefault(d, 0L);
            list.add(new PracticeTrendDayDto(d.format(TREND_LABEL), c));
        }
        return list;
    }

    private List<ActivityItemDto> buildActivity() {
        List<ActivityItemDto> items = new ArrayList<>();

        List<User> recentUsers = userRepository.findTop8ByOrderByCreatedAtDesc();
        for (User u : recentUsers) {
            if (u.getCreatedAt() == null) {
                continue;
            }
            items.add(new ActivityItemDto(
                    "register",
                    "新用户「" + u.getUsername() + "」完成注册",
                    u.getCreatedAt()));
        }

        List<PracticeSession> sessions = practiceSessionRepository.findTop15ByOrderByCreatedAtDesc();
        Set<Long> userIds = new HashSet<>();
        for (PracticeSession ps : sessions) {
            userIds.add(ps.getUserId());
        }
        Map<Long, String> names = new HashMap<>();
        if (!userIds.isEmpty()) {
            userRepository.findAllById(userIds).forEach(u -> names.put(u.getId(), u.getUsername()));
        }

        for (PracticeSession ps : sessions) {
            if (ps.getCreatedAt() == null) {
                continue;
            }
            String name = names.getOrDefault(ps.getUserId(), "用户#" + ps.getUserId());
            String acc = ps.getAccuracy() != null ? ps.getAccuracy() : "—";
            items.add(new ActivityItemDto(
                    "practice",
                    "「" + name + "」完成一次练习（共 " + ps.getTotalCount() + " 题，正确率 " + acc + "）",
                    ps.getCreatedAt()));
        }

        return items.stream()
                .sorted(Comparator.comparing(ActivityItemDto::getOccurredAt,
                        Comparator.nullsLast(Comparator.naturalOrder())).reversed())
                .limit(18)
                .collect(Collectors.toList());
    }
}
