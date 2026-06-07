package com.neuroflow.service;

import com.neuroflow.dao.AssignmentDAO;
import com.neuroflow.model.Assignment;
import java.io.*;
import java.time.LocalDate;
import java.util.List;

public class AnalyticsService {
    private static AnalyticsService instance;
    private final AssignmentDAO assignDAO = new AssignmentDAO();

    private AnalyticsService() {}

    public static AnalyticsService get() {
        if (instance == null) instance = new AnalyticsService();
        return instance;
    }

    public List<Assignment> getWeeklyAssignments(int educatorId) {
        return assignDAO.findByEducatorAndWeek(educatorId, getMonday());
    }

    public void toggleAssignment(int id, boolean completed) {
        assignDAO.setCompleted(id, completed);
    }

    public void addAssignment(Assignment a) {
        assignDAO.insert(a);
    }

    /** Export CSV report of all students and their session data */
    public String exportCsvReport(List<com.neuroflow.model.Student> students) throws IOException {
        SessionService ss = SessionService.get();
        File f = new File("NeuroFlow_Report_" + LocalDate.now() + ".csv");
        try (PrintWriter pw = new PrintWriter(new FileWriter(f))) {
            pw.println("Student,Progress%,Streak,Primary Issue,Trend,Sessions Today,Duration(s)");
            for (com.neuroflow.model.Student s : students) {
                int dur  = ss.sumDurationToday(s.getId());
                int sess = ss.getTodaySessions(s.getId()).size();
                pw.printf("%s,%d,%d,%s,%s,%d,%d%n",
                    s.getName(), s.getWeeklyProgress(), s.getStreakDays(),
                    s.getPrimaryIssue(), s.getTrend(), sess, dur);
            }
        }
        return f.getAbsolutePath();
    }

    private LocalDate getMonday() {
        LocalDate d = LocalDate.now();
        while (d.getDayOfWeek().getValue() != 1) d = d.minusDays(1);
        return d;
    }
}
