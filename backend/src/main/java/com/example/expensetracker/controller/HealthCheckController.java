package com.example.expensetracker.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/health")
public class HealthCheckController {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @GetMapping("/db")
    public String checkDbConnection() {
        try {
            jdbcTemplate.queryForObject("SELECT 1", Integer.class);
            return "Database connection is working!";
        } catch (Exception e) {
            return "Database connection failed: " + e.getMessage();
        }
    }
}
