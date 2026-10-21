package com.example.schoollife.controller;

import com.example.schoollife.entity.Course;
import com.example.schoollife.repository.CourseRepository;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class CourseController {

    private final CourseRepository courseRepo;

    public CourseController(CourseRepository courseRepo) {
        this.courseRepo = courseRepo;
    }

    @GetMapping("/api/courses/{studentNo}")
    public List<Course> list(@PathVariable String studentNo) {
        return courseRepo.findByStudentNo(studentNo);
    }

    @PostMapping("/api/courses")
    public Course add(@RequestBody Course course) {
        course.setId(null);
        return courseRepo.save(course);
    }

    @PutMapping("/api/courses")
    public Course update(@RequestBody Course course) {
        return courseRepo.save(course);
    }

    @DeleteMapping("/api/courses/{id}")
    public void delete(@PathVariable Long id) {
        courseRepo.deleteById(id);
    }
}
