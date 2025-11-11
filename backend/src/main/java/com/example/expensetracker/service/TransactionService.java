package com.example.expensetracker.service;

import com.example.expensetracker.dto.SummaryDTO;
import com.example.expensetracker.entity.Transaction;
import com.example.expensetracker.entity.TransactionType;
import com.example.expensetracker.repository.TransactionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Objects;

@Service
@Transactional(readOnly = true)
public class TransactionService {

    private final TransactionRepository transactionRepository;

    @Autowired
    public TransactionService(TransactionRepository transactionRepository) {
        this.transactionRepository = Objects.requireNonNull(transactionRepository, "TransactionRepository cannot be null");
    }

    public List<Transaction> getAllTransactions() {
        return transactionRepository.findAll();
    }

    @Transactional
    public Transaction addTransaction(Transaction transaction) {
        Objects.requireNonNull(transaction, "Transaction cannot be null");
        return transactionRepository.save(transaction);
    }

    @Transactional
    public void deleteTransaction(Long id) {
        Objects.requireNonNull(id, "Transaction ID cannot be null");
        if (!transactionRepository.existsById(id)) {
            throw new IllegalArgumentException("Transaction with ID " + id + " not found");
        }
        transactionRepository.deleteById(id);
    }

    public SummaryDTO getSummary() {
        BigDecimal totalIncome = transactionRepository.sumByType(TransactionType.INCOME);
        BigDecimal totalExpense = transactionRepository.sumByType(TransactionType.EXPENSE);
        BigDecimal balance = (totalIncome != null ? totalIncome : BigDecimal.ZERO)
                .subtract(totalExpense != null ? totalExpense : BigDecimal.ZERO);

        return new SummaryDTO(
            totalIncome != null ? totalIncome : BigDecimal.ZERO,
            totalExpense != null ? totalExpense : BigDecimal.ZERO,
            balance
        );
    }

    public SummaryDTO getSummaryByPeriod(LocalDate startDate, LocalDate endDate) {
        Objects.requireNonNull(startDate, "Start date cannot be null");
        Objects.requireNonNull(endDate, "End date cannot be null");
        
        if (startDate.isAfter(endDate)) {
            throw new IllegalArgumentException("Start date cannot be after end date");
        }

        LocalDateTime startDateTime = startDate.atStartOfDay();
        LocalDateTime endDateTime = endDate.atTime(LocalTime.MAX);

        BigDecimal totalIncome = transactionRepository.sumByTypeAndDateBetween(
            TransactionType.INCOME, startDateTime, endDateTime);
        BigDecimal totalExpense = transactionRepository.sumByTypeAndDateBetween(
            TransactionType.EXPENSE, startDateTime, endDateTime);
            
        BigDecimal balance = (totalIncome != null ? totalIncome : BigDecimal.ZERO)
                .subtract(totalExpense != null ? totalExpense : BigDecimal.ZERO);

        return new SummaryDTO(
            totalIncome != null ? totalIncome : BigDecimal.ZERO,
            totalExpense != null ? totalExpense : BigDecimal.ZERO,
            balance
        );
    }

    // --- NEW METHOD 2 ---
    public List<Transaction> getTransactionsByPeriod(LocalDate startDate, LocalDate endDate) {
        LocalDateTime startDateTime = startDate.atStartOfDay();
        LocalDateTime endDateTime = endDate.atTime(LocalTime.MAX);
        return transactionRepository.findByDateBetween(startDateTime, endDateTime);
    }
}