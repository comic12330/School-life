package com.example.school_life.net.model;

import java.util.List;

/** GET /api/edu/{studentNo} 的返回体 */
public class EduResult {
    public User profile;
    public List<Grade> grades;
}
