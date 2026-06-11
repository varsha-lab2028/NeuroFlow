package com.neuroflow.service;

import com.neuroflow.dao.NoneEventDAO;
import com.neuroflow.model.NoneEvent;
import java.util.List;

public class NoneEventService {

    private static NoneEventService instance;
    private final NoneEventDAO dao = new NoneEventDAO();

    private NoneEventService() {}

    public static NoneEventService get() {
        if (instance == null) instance = new NoneEventService();
        return instance;
    }

    /**
     * Called when Python reports a NONE detection for a student.
     */
    public void recordNoneEvent(int studentId) {
        dao.incrementToday(studentId);
        System.out.println("[NoneEvent] Recorded NONE for student " + studentId);
    }

    /**
     * Returns last 30 days of NONE counts for the trend graph.
     */
    public List<NoneEvent> getTrend(int studentId) {
        return dao.getRecentDays(studentId, 30);
    }

    /**
     * Returns all history for a student.
     */
    public List<NoneEvent> getFullHistory(int studentId) {
        return dao.getAllForStudent(studentId);
    }

    /**
     * Calculates trend direction over the last 7 days.
     * Returns "improving", "stable", or "needs_attention"
     */
    public String getTrendDirection(int studentId) {
        List<NoneEvent> recent = dao.getRecentDays(studentId, 7);
        if (recent.size() < 2) return "stable";

        // Compare first half average vs second half average
        int mid   = recent.size() / 2;
        double firstHalf  = recent.subList(0, mid).stream()
                .mapToInt(NoneEvent::getCount).average().orElse(0);
        double secondHalf = recent.subList(mid, recent.size()).stream()
                .mapToInt(NoneEvent::getCount).average().orElse(0);

        if (secondHalf < firstHalf * 0.85)  return "improving";
        if (secondHalf > firstHalf * 1.15)  return "needs_attention";
        return "stable";
    }
}
