package com.example.finance_data_processing_and_access_control.controller;

import com.example.finance_data_processing_and_access_control.dto.response.ApiResponse;
import com.example.finance_data_processing_and_access_control.dto.response.DashboardSummaryResponse;
import com.example.finance_data_processing_and_access_control.dto.response.FinancialRecordResponse;
import com.example.finance_data_processing_and_access_control.service.DashboardService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/dashboard")
@RequiredArgsConstructor
@Tag(name = "Dashboard", description = "Summary and analytics endpoints")
@SecurityRequirement(name = "bearerAuth")
public class DashboardController {

    private final DashboardService dashboardService;

    @GetMapping("/summary")
    @PreAuthorize("hasRole('ADMIN') or hasRole('ANALYST') or hasRole('VIEWER')")
    @Operation(summary = "Full dashboard summary — ALL roles")
    public ResponseEntity<ApiResponse<DashboardSummaryResponse>> getSummary() {
        return ResponseEntity.ok(ApiResponse.success(dashboardService.getSummary()));
    }

    @GetMapping("/total-income")
    @PreAuthorize("hasRole('ADMIN') or hasRole('ANALYST') or hasRole('VIEWER')")
    @Operation(summary = "Get total income — ALL roles")
    public ResponseEntity<ApiResponse<BigDecimal>> getTotalIncome() {
        return ResponseEntity.ok(ApiResponse.success(dashboardService.getTotalIncome()));
    }

    @GetMapping("/total-expenses")
    @PreAuthorize("hasRole('ADMIN') or hasRole('ANALYST') or hasRole('VIEWER')")
    @Operation(summary = "Get total expenses — ALL roles")
    public ResponseEntity<ApiResponse<BigDecimal>> getTotalExpenses() {
        return ResponseEntity.ok(ApiResponse.success(dashboardService.getTotalExpenses()));
    }

    @GetMapping("/net-balance")
    @PreAuthorize("hasRole('ADMIN') or hasRole('ANALYST') or hasRole('VIEWER')")
    @Operation(summary = "Get net balance — ALL roles")
    public ResponseEntity<ApiResponse<BigDecimal>> getNetBalance() {
        return ResponseEntity.ok(ApiResponse.success(dashboardService.getNetBalance()));
    }

    @GetMapping("/category-totals")
    @PreAuthorize("hasRole('ADMIN') or hasRole('ANALYST')")
    @Operation(summary = "Get category-wise totals — ADMIN, ANALYST only")
    public ResponseEntity<ApiResponse<List<DashboardSummaryResponse.CategoryTotalResponse>>> getCategoryTotals() {
        return ResponseEntity.ok(ApiResponse.success(dashboardService.getCategoryTotals()));
    }

    @GetMapping("/recent-activity")
    @PreAuthorize("hasRole('ADMIN') or hasRole('ANALYST') or hasRole('VIEWER')")
    @Operation(summary = "Get recent transactions — ALL roles")
    public ResponseEntity<ApiResponse<List<FinancialRecordResponse>>> getRecentActivity(
            @RequestParam(defaultValue = "10") int limit) {
        return ResponseEntity.ok(ApiResponse.success(dashboardService.getRecentActivity(limit)));
    }

    @GetMapping("/monthly-trend")
    @PreAuthorize("hasRole('ADMIN') or hasRole('ANALYST')")
    @Operation(summary = "Get monthly income/expense trend — ADMIN, ANALYST only")
    public ResponseEntity<ApiResponse<List<DashboardSummaryResponse.MonthlyTrendResponse>>> getMonthlyTrend(
            @RequestParam(defaultValue = "6") int months) {
        return ResponseEntity.ok(ApiResponse.success(dashboardService.getMonthlyTrend(months)));
    }
}