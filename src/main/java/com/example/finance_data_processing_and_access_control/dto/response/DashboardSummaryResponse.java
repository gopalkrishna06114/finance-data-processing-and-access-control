package com.example.finance_data_processing_and_access_control.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DashboardSummaryResponse {

    private BigDecimal totalIncome;
    private BigDecimal totalExpenses;
    private BigDecimal netBalance;
    private List<CategoryTotalResponse> categoryTotals;
    private List<FinancialRecordResponse> recentActivity;
    private List<MonthlyTrendResponse> monthlyTrend;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CategoryTotalResponse {
        private String category;
        private String type;
        private BigDecimal total;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MonthlyTrendResponse {
        private int year;
        private int month;
        private String monthName;
        private BigDecimal income;
        private BigDecimal expenses;
        private BigDecimal net;
    }
}