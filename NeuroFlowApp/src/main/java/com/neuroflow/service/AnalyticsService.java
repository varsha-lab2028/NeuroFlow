package com.neuroflow.service;

import com.neuroflow.dao.ErrorPatternDao;
import com.neuroflow.dao.PracticeActivityDao;
import com.neuroflow.dao.StudentDao;
import com.neuroflow.model.PracticeActivity;
import com.neuroflow.model.Student;
import java.io.*;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

public class AnalyticsService {

    private static AnalyticsService instance;
    private final PracticeActivityDao activityDao = new PracticeActivityDao();
    private final ErrorPatternDao errorPatternDao = new ErrorPatternDao();
    private final StudentDao studentDao = new StudentDao();

    private AnalyticsService() {}

    public static AnalyticsService get() {
        if (instance == null) instance = new AnalyticsService();
        return instance;
    }

    public List<PracticeActivity> getWeeklyActivities(int educatorId) {
        return activityDao.findByEducatorId(educatorId);
    }

    public void toggleActivity(int id, boolean completed) {
        activityDao.markCompleted(id, completed);
    }

    public void addActivity(PracticeActivity a) {
        activityDao.insert(a);
    }

    public Map<String, Integer> getWeeklyErrorTotals() {
        return errorPatternDao.weeklyTotals(getMonday());
    }

    public Map<String, Integer> getAllTimeErrorTotals() {
        return errorPatternDao.allTimeTotals();
    }

    public int getActiveStudentCount() {
        return studentDao.countActive();
    }

    public int getPracticedTodayCount() {
        return studentDao.countPracticedToday();
    }

    public String exportCsvReport(List<Student> students) throws IOException {
        PracticeSessionService ss = PracticeSessionService.get();
        File f = new File("NeuroFlow_Report_" + LocalDate.now() + ".csv");
        try (PrintWriter pw = new PrintWriter(new FileWriter(f))) {
            pw.println("Student,Progress%,Streak,Primary Issue,Trend,Sessions Today,Duration(s)");
            for (Student s : students) {
                int dur  = ss.sumDurationToday(s.getStudentId());
                int sess = ss.getTodaySessions(s.getStudentId()).size();
                pw.printf("%s,%d,%d,%s,%s,%d,%d%n",
                        s.getName(), s.getWeeklyProgress(), s.getStreakDays(),
                        s.getPrimaryIssue(), s.getTrend(), sess, dur);
            }
        }
        return f.getAbsolutePath();
    }

    private String getMonday() {
        LocalDate d = LocalDate.now();
        while (d.getDayOfWeek().getValue() != 1) d = d.minusDays(1);
        return d.toString();
    }
}