package com.example.schoollife.controller;

import com.example.schoollife.entity.User;
import com.example.schoollife.repository.UserRepository;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
public class AuthController {

    private final UserRepository userRepo;

    public AuthController(UserRepository userRepo) {
        this.userRepo = userRepo;
    }

    @PostMapping("/api/login")
    public User login(@RequestBody Map<String, String> body) {
        return userRepo.findByStudentNoAndPassword(body.get("studentNo"), body.get("password"));
    }
}
