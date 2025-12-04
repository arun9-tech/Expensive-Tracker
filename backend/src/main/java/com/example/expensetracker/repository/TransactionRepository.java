package com.example.expensetracker.repository;

import com.example.expensetracker.entity.Transaction;
import com.example.expensetracker.entity.TransactionType;
import com.example.expensetracker.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long> {
    
    @Query("SELECT COALESCE(SUM(t.amount), 0) FROM Transaction t WHERE t.type = :type")
    BigDecimal sumByType(@Param("type") TransactionType type);
    
    @Query("SELECT COALESCE(SUM(t.amount), 0) FROM Transaction t WHERE t.type = :type AND t.date BETWEEN :startDate AND :endDate")
    BigDecimal sumByTypeAndDateBetween(
        @Param("type") TransactionType type,
        @Param("startDate") LocalDateTime startDate,
        @Param("endDate") LocalDateTime endDate
    );
    
    List<Transaction> findByDateBetween(LocalDateTime startDate, LocalDateTime endDate);
    
    @Query("SELECT t FROM Transaction t WHERE t.user = :user")
    List<Transaction> findByUser(@Param("user") User user);
    
    @Query("SELECT COALESCE(SUM(t.amount), 0) FROM Transaction t WHERE t.type = :type AND t.user = :user")
    BigDecimal sumByTypeAndUser(
        @Param("type") TransactionType type,
        @Param("user") User user
    );
    
    @Query("SELECT COALESCE(SUM(t.amount), 0) FROM Transaction t WHERE t.type = :type AND t.user = :user AND t.date BETWEEN :startDate AND :endDate")
    BigDecimal sumByTypeAndUserAndDateBetween(
        @Param("type") TransactionType type,
        @Param("user") User user,
        @Param("startDate") LocalDateTime startDate,
        @Param("endDate") LocalDateTime endDate
    );
    
    @Query("SELECT t FROM Transaction t WHERE t.user = :user AND t.date BETWEEN :startDate AND :endDate")
    List<Transaction> findByUserAndDateBetween(
        @Param("user") User user,
        @Param("startDate") LocalDateTime startDate,
        @Param("endDate") LocalDateTime endDate
    );
}
