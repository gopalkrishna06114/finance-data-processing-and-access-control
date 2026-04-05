package com.example.finance_data_processing_and_access_control.repository;

import com.example.finance_data_processing_and_access_control.entity.FinancialRecord;
import com.example.finance_data_processing_and_access_control.enums.TransactionType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface FinancialRecordRepository extends JpaRepository<FinancialRecord, Long> {

    // Find active (non-deleted) record by id
    Optional<FinancialRecord> findByIdAndDeletedFalse(Long id);

    // Paginated filtered list
    @Query("""
        SELECT r FROM FinancialRecord r
        WHERE r.deleted = false
          AND (:type IS NULL OR r.type = :type)
          AND (:category IS NULL OR LOWER(r.category) = LOWER(:category))
          AND (:startDate IS NULL OR r.date >= :startDate)
          AND (:endDate IS NULL OR r.date <= :endDate)
        ORDER BY r.date DESC
    """)
    Page<FinancialRecord> findAllWithFilters(
            @Param("type") TransactionType type,
            @Param("category") String category,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate,
            Pageable pageable
    );

    // Total income or total expenses
    @Query("SELECT COALESCE(SUM(r.amount), 0) FROM FinancialRecord r WHERE r.deleted = false AND r.type = :type")
    BigDecimal sumByType(@Param("type") TransactionType type);

    // Category wise totals
    @Query("""
        SELECT r.category, r.type, SUM(r.amount)
        FROM FinancialRecord r
        WHERE r.deleted = false
        GROUP BY r.category, r.type
        ORDER BY SUM(r.amount) DESC
    """)
    List<Object[]> getCategoryWiseTotals();

    // Monthly trend for last N months
    @Query("""
        SELECT YEAR(r.date), MONTH(r.date), r.type, SUM(r.amount)
        FROM FinancialRecord r
        WHERE r.deleted = false AND r.date >= :startDate
        GROUP BY YEAR(r.date), MONTH(r.date), r.type
        ORDER BY YEAR(r.date), MONTH(r.date)
    """)
    List<Object[]> getMonthlyTrend(@Param("startDate") LocalDate startDate);

    // Recent activity
    @Query("SELECT r FROM FinancialRecord r WHERE r.deleted = false ORDER BY r.createdAt DESC")
    List<FinancialRecord> findRecentActivity(Pageable pageable);
}