package com.neuroflow.service;

import com.neuroflow.dao.StudentDao;
import com.neuroflow.model.Student;
import java.util.List;

public class StudentService {

    private static StudentService instance;
    private final StudentDao studentDao = new StudentDao();

    private StudentService() {}
    public static StudentService get() {
        if (instance == null) instance = new StudentService();
        return instance;
    }

    public Student getByUserId(int userId) {
        return studentDao.findByUserId(userId);
    }

    public List<Student> getByEducatorId(int edId) {
        return studentDao.findByEducatorId(edId);
    }

    public List<Student> getByParentId(int parentId) {
        return studentDao.findByParentId(parentId);
    }

    public List<Student> getAllStudents() {
        return studentDao.findAll();
    }

    public Student getById(int id) {
        return studentDao.findById(id);
    }

    public void save(Student s) {
        if (s.getStudentId() == 0) studentDao.insert(s);
        else studentDao.update(s);
    }

    public int countActive() {
        return studentDao.countActive();
    }

    public int countPracticedToday() {
        return studentDao.countPracticedToday();
    }

    public void updateCurrentLetter(Student s, String letter) {
        s.setCurrentLetter(letter);
        studentDao.update(s);
    }

    public void incrementStreak(Student s) {
        s.setStreakDays(s.getStreakDays() + 1);
        studentDao.update(s);
    }
}