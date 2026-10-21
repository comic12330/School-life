package com.example.schoollife.repository;

import com.example.schoollife.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Integer> {
    User findByStudentNoAndPassword(String studentNo, String password);
    User findByStudentNo(String studentNo);
}
