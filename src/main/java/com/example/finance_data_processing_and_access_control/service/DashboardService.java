package com.example.finance_data_processing_and_access_control.service;

import com.example.finance_data_processing_and_access_control.dto.response.DashboardSummaryResponse;
import com.example.finance_data_processing_and_access_control.dto.response.FinancialRecordResponse;
import com.example.finance_data_processing_and_access_control.enums.TransactionType;
import com.example.finance_data_processing_and_access_control.repository.FinancialRecordRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.Month;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DashboardService {

    private final FinancialRecordRepository financialRecordRepository;
    private final FinancialRecordService financialRecordService;

    // Full dashboard summary
    public DashboardSummaryResponse getSummary() {
        BigDecimal totalIncome   = financialRecordRepository.sumByType(TransactionType.INCOME);
        BigDecimal totalExpenses = financialRecordRepository.sumByType(TransactionType.EXPENSE);
        BigDecimal netBalance    = totalIncome.subtract(totalExpenses);

        return DashboardSummaryResponse.builder()
                .totalIncome(totalIncome)
                .totalExpenses(totalExpenses)
                .netBalance(netBalance)
                .categoryTotals(getCategoryTotals())
                .recentActivity(getRecentActivity(10))
                .monthlyTrend(getMonthlyTrend(6))
                .build();
    }

    // Total income
    public BigDecimal getTotalIncome() {
        return financialRecordRepository.sumByType(TransactionType.INCOME);
    }

    // Total expenses
    public BigDecimal getTotalExpenses() {
        return financialRecordRepository.sumByType(TransactionType.EXPENSE);
    }

    // Net balance
    public BigDecimal getNetBalance() {
        return getTotalIncome().subtract(getTotalExpenses());
    }

    // Category wise totals
    public List<DashboardSummaryResponse.CategoryTotalResponse> getCategoryTotals() {
        return financialRecordRepository.getCategoryWiseTotals()
                .stream()
                .map(row -> DashboardSummaryResponse.CategoryTotalResponse.builder()
                        .category((String) row[0])
                        .type(row[1].toString())
                        .total((BigDecimal) row[2])
                        .build())
                .collect(Collectors.toList());
    }

    // Recent activity (last N records)
    public List<FinancialRecordResponse> getRecentActivity(int limit) {
        return financialRecordRepository
                .findRecentActivity(PageRequest.of(0, limit))
                .stream()
                .map(financialRecordService::mapToResponse)
                .collect(Collectors.toList());
    }

    // Monthly trend for last N months
    public List<DashboardSummaryResponse.MonthlyTrendResponse> getMonthlyTrend(int months) {
        LocalDate startDate = LocalDate.now().minusMonths(months).withDayOfMonth(1);
        List<Object[]> rows = financialRecordRepository.getMonthlyTrend(startDate);

        // Group by year+month, then combine income and expense into one entry
        Map<String, DashboardSummaryResponse.MonthlyTrendResponse> trendMap = new HashMap<>();

        for (Object[] row : rows) {
            int year      = ((Number) row[0]).intValue();
            int month     = ((Number) row[1]).intValue();
            String type   = row[2].toString();
            BigDecimal amt = (BigDecimal) row[3];
            String key    = year + "-" + month;

            trendMap.computeIfAbsent(key, k -> DashboardSummaryResponse.MonthlyTrendResponse.builder()
                    .year(year)
                    .month(month)
                    .monthName(Month.of(month).name())
                    .income(BigDecimal.ZERO)
                    .expenses(BigDecimal.ZERO)
                    .net(BigDecimal.ZERO)
                    .build());

            DashboardSummaryResponse.MonthlyTrendResponse entry = trendMap.get(key);

            if (type.equals(TransactionType.INCOME.name())) {
                entry.setIncome(amt);
            } else {
                entry.setExpenses(amt);
            }
            entry.setNet(entry.getIncome().subtract(entry.getExpenses()));
        }

        return new ArrayList<>(trendMap.values())
                .stream()
                .sorted((a, b) -> {
                    if (a.getYear() != b.getYear()) return Integer.compare(a.getYear(), b.getYear());
                    return Integer.compare(a.getMonth(), b.getMonth());
                })
                .collect(Collectors.toList());
    }
}