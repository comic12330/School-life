package com.example.school_life.net.model;

import java.util.List;

/** GET /api/card/{studentNo} 的返回体 */
public class CardResult {
    public CampusCard card;
    public List<CardTransaction> transactions;
}
