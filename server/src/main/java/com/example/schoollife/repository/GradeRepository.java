package com.example.schoollife.repository;

import com.example.schoollife.entity.Grade;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface GradeRepository extends JpaRepository<Grade, Long> {
    List<Grade> findByStudentNo(String studentNo);
}
