package com.neuroflow.service;

import com.neuroflow.dao.StudentDAO;
import com.neuroflow.model.Student;
import java.util.List;

public class StudentService {
    private static StudentService instance;
    private final StudentDAO studentDAO = new StudentDAO();

    private StudentService() {}

    public static StudentService get() {
        if (instance == null) instance = new StudentService();
        return instance;
    }

    public Student getByUserId(int userId) {
        return studentDAO.findByUserId(userId);
    }

    public List<Student> getByEducatorId(int edId) {
        return studentDAO.findByEducatorId(edId);
    }

    public List<Student> getByParentId(int parentId) {
        return studentDAO.findByParentId(parentId);
    }

    public List<Student> getAllStudents() {
        return studentDAO.findAll();
    }

    public Student getById(int id) {
        return studentDAO.findById(id);
    }

    public void save(Student s) {
        if (s.getId() == 0) studentDAO.insert(s);
        else studentDAO.update(s);
    }

    public int countActive() {
        return studentDAO.countActive();
    }

    public int countPracticedToday() {
        return studentDAO.countPracticedToday();
    }

    public void updateCurrentLetter(Student s, String letter) {
        s.setCurrentLetter(letter);
        studentDAO.update(s);
    }

    public void incrementStreak(Student s) {
        s.setStreakDays(s.getStreakDays() + 1);
        studentDAO.update(s);
    }
}
