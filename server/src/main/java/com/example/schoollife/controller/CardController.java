package com.example.schoollife.controller;

import com.example.schoollife.entity.CampusCard;
import com.example.schoollife.entity.CardTransaction;
import com.example.schoollife.repository.CampusCardRepository;
import com.example.schoollife.repository.CardTransactionRepository;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.LinkedHashMap;
import java.util.Map;

@RestController
public class CardController {

    private final CampusCardRepository cardRepo;
    private final CardTransactionRepository txRepo;

    public CardController(CampusCardRepository cardRepo, CardTransactionRepository txRepo) {
        this.cardRepo = cardRepo;
        this.txRepo = txRepo;
    }

    @GetMapping("/api/card/{studentNo}")
    public Map<String, Object> card(@PathVariable String studentNo) {
        CampusCard card = cardRepo.findByStudentNo(studentNo);
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("card", card);
        result.put("transactions", txRepo.findByStudentNoOrderByTimeDesc(studentNo));
        return result;
    }

    @PostMapping("/api/card/{studentNo}/lost")
    public CampusCard setLost(@PathVariable String studentNo, @RequestParam boolean lost) {
        CampusCard card = cardRepo.findByStudentNo(studentNo);
        card.setLost(lost);
        card.setUpdatedAt(System.currentTimeMillis());
        cardRepo.save(card);

        CardTransaction tx = new CardTransaction();
        tx.setStudentNo(studentNo);
        tx.setAmount(new java.math.BigDecimal("0"));
        tx.setType(lost ? "挂失" : "解挂");
        tx.setPlace("手机端操作");
        tx.setTime(System.currentTimeMillis());
        txRepo.save(tx);
        return card;
    }
}
