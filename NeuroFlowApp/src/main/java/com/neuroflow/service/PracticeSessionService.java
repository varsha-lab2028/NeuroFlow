package com.neuroflow.service;

import com.neuroflow.dao.ErrorPatternDao;
import com.neuroflow.dao.PracticeSessionDao;
import com.neuroflow.model.ErrorPattern;
import com.neuroflow.model.PracticeSession;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

public class PracticeSessionService {
    private static PracticeSessionService instance;
    private final PracticeSessionDao sessionDao = new PracticeSessionDao();
    private final ErrorPatternDao errorDao = new ErrorPatternDao();

    private PracticeSessionService() {}
    public static PracticeSessionService get() {
        if (instance == null) instance = new PracticeSessionService();
        return instance;
    }

    public int save(PracticeSession s) {
        int id = sessionDao.insert(s);
        if (!s.isCorrect() && s.getDetectedLetter() != null) {
            String errType = resolveErrorType(s.getTargetLetter(), s.getDetectedLetter());
            ErrorPattern ep = new ErrorPattern(0, s.getStudentId(), errType,
                    LocalDate.now().toString(), 1);
            errorDao.upsert(ep);
        }
        return id;
    }

    private String resolveErrorType(String target, String detected) {
        boolean bothGroup1 = (target.equals("b") || target.equals("d"))
                && (detected.equals("b") || detected.equals("d"));
        boolean bothGroup2 = (target.equals("p") || target.equals("q"))
                && (detected.equals("p") || detected.equals("q"));
        if (bothGroup1) return "b/d reversal";
        if (bothGroup2) return "p/q reversal";
        return "stroke direction";
    }

    public List<PracticeSession> getTodaySessions(int studentId) {
        return sessionDao.findTodayByStudentId(studentId);
    }

    public List<PracticeSession> getAllSessions(int studentId) {
        return sessionDao.findByStudentId(studentId);
    }

    public List<PracticeSession> getRecentSessions(int studentId, int limit) {
        return sessionDao.findRecentByStudentId(studentId, limit);
    }

    public int sumDurationToday(int studentId) {
        return sessionDao.sumDurationToday(studentId);
    }

    public int countTodayAttempts(int studentId) {
        return sessionDao.countTodayAttempts(studentId);
    }

    public int countThisWeek(int studentId) {
        return sessionDao.countThisWeek(studentId);
    }

    public int[] weeklyPracticeDays(int studentId) {
        return sessionDao.weeklyPracticeDays(studentId);
    }

    public Map<String, Integer> weeklyErrorTotals() {
        return errorDao.weeklyTotals(LocalDate.now().toString());
    }

    public Map<String, Integer> allTimeErrorTotals() {
        return errorDao.allTimeTotals();
    }
}