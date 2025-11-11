package com.example.expensetracker.controller;

import com.example.expensetracker.dto.SummaryDTO;
import com.example.expensetracker.entity.Transaction;
import com.example.expensetracker.service.TransactionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/transactions")
@CrossOrigin(origins = {"http://localhost:5173", "http://localhost:3000"})
public class TransactionController {

    private final TransactionService transactionService;

    @Autowired
    public TransactionController(TransactionService transactionService) {
        this.transactionService = transactionService;
    }

    @GetMapping
    public ResponseEntity<List<Transaction>> getAllTransactions() {
        try {
            return ResponseEntity.ok(transactionService.getAllTransactions());
        } catch (Exception e) {
            throw new ResponseStatusException(
                HttpStatus.INTERNAL_SERVER_ERROR, 
                "Error retrieving transactions: " + e.getMessage(), 
                e
            );
        }
    }

    @PostMapping
    public ResponseEntity<Transaction> addTransaction(@RequestBody Transaction transaction) {
        try {
            Transaction savedTransaction = transactionService.addTransaction(transaction);
            return ResponseEntity.status(HttpStatus.CREATED).body(savedTransaction);
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(
                HttpStatus.BAD_REQUEST, 
                "Invalid transaction data: " + e.getMessage(), 
                e
            );
        } catch (Exception e) {
            throw new ResponseStatusException(
                HttpStatus.INTERNAL_SERVER_ERROR, 
                "Error creating transaction: " + e.getMessage(), 
                e
            );
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTransaction(@PathVariable Long id) {
        try {
            transactionService.deleteTransaction(id);
            return ResponseEntity.noContent().build();
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(
                HttpStatus.NOT_FOUND, 
                e.getMessage(), 
                e
            );
        } catch (Exception e) {
            throw new ResponseStatusException(
                HttpStatus.INTERNAL_SERVER_ERROR, 
                "Error deleting transaction: " + e.getMessage(), 
                e
            );
        }
    }

    @GetMapping("/summary")
    public ResponseEntity<SummaryDTO> getSummary() {
        try {
            return ResponseEntity.ok(transactionService.getSummary());
        } catch (Exception e) {
            throw new ResponseStatusException(
                HttpStatus.INTERNAL_SERVER_ERROR, 
                "Error generating summary: " + e.getMessage(), 
                e
            );
        }
    }

    @GetMapping("/summary-by-period")
    public ResponseEntity<SummaryDTO> getSummaryByPeriod(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        try {
            return ResponseEntity.ok(transactionService.getSummaryByPeriod(startDate, endDate));
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(
                HttpStatus.BAD_REQUEST, 
                e.getMessage(), 
                e
            );
        } catch (Exception e) {
            throw new ResponseStatusException(
                HttpStatus.INTERNAL_SERVER_ERROR, 
                "Error generating period summary: " + e.getMessage(), 
                e
            );
        }
    }

    @GetMapping("/filter")
    public ResponseEntity<List<Transaction>> getTransactionsByPeriod(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        try {
            if (startDate == null || endDate == null) {
                throw new IllegalArgumentException("Both startDate and endDate parameters are required");
            }
            if (startDate.isAfter(endDate)) {
                throw new IllegalArgumentException("startDate cannot be after endDate");
            }
            List<Transaction> transactions = transactionService.getTransactionsByPeriod(startDate, endDate);
            return ResponseEntity.ok(transactions);
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                e.getMessage(),
                e
            );
        } catch (Exception e) {
            throw new ResponseStatusException(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "Error retrieving transactions: " + e.getMessage(),
                e
            );
        }
    }
}