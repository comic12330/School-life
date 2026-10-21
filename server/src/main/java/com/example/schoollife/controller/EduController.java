package com.example.schoollife.controller;

import com.example.schoollife.entity.User;
import com.example.schoollife.repository.GradeRepository;
import com.example.schoollife.repository.UserRepository;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.util.LinkedHashMap;
import java.util.Map;

@RestController
public class EduController {

    private final UserRepository userRepo;
    private final GradeRepository gradeRepo;

    public EduController(UserRepository userRepo, GradeRepository gradeRepo) {
        this.userRepo = userRepo;
        this.gradeRepo = gradeRepo;
    }

    @GetMapping("/api/edu/{studentNo}")
    public Map<String, Object> edu(@PathVariable String studentNo) {
        User profile = userRepo.findByStudentNo(studentNo);
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("profile", profile);
        result.put("grades", gradeRepo.findByStudentNo(studentNo));
        return result;
    }
}
