package com.neuroflow.service;

import com.neuroflow.dao.ErrorPatternDAO;
import com.neuroflow.dao.PracticeSessionDAO;
import com.neuroflow.model.ErrorPattern;
import com.neuroflow.model.PracticeSession;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

public class SessionService {
    private static SessionService instance;
    private final PracticeSessionDAO sessionDAO = new PracticeSessionDAO();
    private final ErrorPatternDAO errorDAO = new ErrorPatternDAO();

    private SessionService() {}

    public static SessionService get() {
        if (instance == null) instance = new SessionService();
        return instance;
    }

    public int save(PracticeSession s) {
        int id = sessionDAO.insert(s);
        if (!s.isCorrect() && s.getDetectedLetter() != null) {
            String errType = resolveErrorType(s.getTargetLetter(), s.getDetectedLetter());
            ErrorPattern ep = new ErrorPattern(s.getStudentId(), errType, LocalDate.now(), 1);
            errorDAO.upsert(ep);
        }
        return id;
    }

    private String resolveErrorType(String target, String detected) {
        boolean bothGroup1 = (target.equals("b") || target.equals("d")) && (detected.equals("b") || detected.equals("d"));
        boolean bothGroup2 = (target.equals("p") || target.equals("q")) && (detected.equals("p") || detected.equals("q"));
        if (bothGroup1) return "b/d reversal";
        if (bothGroup2) return "p/q reversal";
        return "stroke direction";
    }

    public List<PracticeSession> getTodaySessions(int studentId) {
        return sessionDAO.findTodayByStudentId(studentId);
    }

    public List<PracticeSession> getAllSessions(int studentId) {
        return sessionDAO.findByStudentId(studentId);
    }

    public int sumDurationToday(int studentId) {
        return sessionDAO.sumDurationToday(studentId);
    }

    public int countTodayAttempts(int studentId) {
        return sessionDAO.countTodayAttempts(studentId);
    }

    public int[] weeklyPracticeDays(int studentId) {
        return sessionDAO.weeklyPracticeDays(studentId);
    }

    public Map<String, Integer> weeklyErrorTotals() {
        return errorDAO.weeklyTotals(LocalDate.now());
    }
}
