package com.neuroflow.service;

import com.neuroflow.dao.PracticeActivityDao;
import com.neuroflow.model.PracticeActivity;
import java.time.LocalDate;
import java.util.List;

public class PracticeActivityService {

    private static PracticeActivityService instance;
    private final PracticeActivityDao activityDao = new PracticeActivityDao();

    private PracticeActivityService() {}

    public static PracticeActivityService get() {
        if (instance == null) instance = new PracticeActivityService();
        return instance;
    }

    public List<PracticeActivity> getClassroomFocus(int educatorId) {
        return activityDao.findByEducatorId(educatorId);
    }

    public List<PracticeActivity> getActivitiesForThisWeek() {
        return activityDao.findByWeekDate(getMonday());
    }

    public List<PracticeActivity> getSharedWithParents() {
        return activityDao.findSharedWithParents();
    }

    public List<PracticeActivity> getAll() {
        return activityDao.findAll();
    }

    public int createActivity(PracticeActivity a) {
        if (a.getWeekDate() == null || a.getWeekDate().isBlank()) {
            a.setWeekDate(getMonday());
        }
        if (a.getActivityType() == null || a.getActivityType().isBlank()) {
            a.setActivityType("classroom_focus");
        }
        return activityDao.insert(a);
    }

    public void markCompleted(int id, boolean completed) {
        activityDao.markCompleted(id, completed);
    }

    public void shareWithParents(int id, boolean shared) {
        activityDao.shareWithParents(id, shared);
    }

    public void delete(int id) {
        activityDao.delete(id);
    }

    private String getMonday() {
        LocalDate d = LocalDate.now();
        while (d.getDayOfWeek().getValue() != 1) d = d.minusDays(1);
        return d.toString();
    }
}
