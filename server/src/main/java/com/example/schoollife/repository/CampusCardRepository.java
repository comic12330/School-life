package com.example.schoollife.repository;

import com.example.schoollife.entity.CampusCard;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CampusCardRepository extends JpaRepository<CampusCard, Integer> {
    CampusCard findByStudentNo(String studentNo);
}
