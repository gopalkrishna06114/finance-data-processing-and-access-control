package com.example.finance_data_processing_and_access_control.enums;

public enum Role {
    VIEWER,    // Can only view dashboard data and records
    ANALYST,   // Can view records and access all summary/insights APIs
    ADMIN      // Full access including user management
}