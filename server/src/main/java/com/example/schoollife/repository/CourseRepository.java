package com.example.schoollife.repository;

import com.example.schoollife.entity.Course;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CourseRepository extends JpaRepository<Course, Long> {
    List<Course> findByStudentNo(String studentNo);
}
