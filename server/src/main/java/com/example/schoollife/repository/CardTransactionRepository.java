package com.example.schoollife.repository;

import com.example.schoollife.entity.CardTransaction;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CardTransactionRepository extends JpaRepository<CardTransaction, Long> {
    List<CardTransaction> findByStudentNoOrderByTimeDesc(String studentNo);
}
