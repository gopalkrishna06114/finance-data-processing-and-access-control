package com.example.finance_data_processing_and_access_control.service;

import com.example.finance_data_processing_and_access_control.dto.request.FinancialRecordRequest;
import com.example.finance_data_processing_and_access_control.dto.response.FinancialRecordResponse;
import com.example.finance_data_processing_and_access_control.entity.FinancialRecord;
import com.example.finance_data_processing_and_access_control.entity.User;
import com.example.finance_data_processing_and_access_control.enums.TransactionType;
import com.example.finance_data_processing_and_access_control.exception.ResourceNotFoundException;
import com.example.finance_data_processing_and_access_control.repository.FinancialRecordRepository;
import com.example.finance_data_processing_and_access_control.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.LocalDate;

@Service
@RequiredArgsConstructor
public class FinancialRecordService {

    private final FinancialRecordRepository financialRecordRepository;
    private final UserRepository userRepository;

    // Create a new record
    public FinancialRecordResponse createRecord(FinancialRecordRequest request) {
        User currentUser = getCurrentUser();

        FinancialRecord record = FinancialRecord.builder()
                .amount(request.getAmount())
                .type(request.getType())
                .category(request.getCategory())
                .date(request.getDate())
                .notes(request.getNotes())
                .createdBy(currentUser)
                .deleted(false)
                .build();

        financialRecordRepository.save(record);
        return mapToResponse(record);
    }

    // Get all records with filters and pagination
    public Page<FinancialRecordResponse> getAllRecords(
            TransactionType type,
            String category,
            LocalDate startDate,
            LocalDate endDate,
            Pageable pageable) {

        return financialRecordRepository
                .findAllWithFilters(type, category, startDate, endDate, pageable)
                .map(this::mapToResponse);
    }

    // Get single record by ID
    public FinancialRecordResponse getRecordById(Long id) {
        FinancialRecord record = financialRecordRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("Record not found with id: " + id));
        return mapToResponse(record);
    }

    // Update record
    public FinancialRecordResponse updateRecord(Long id, FinancialRecordRequest request) {
        FinancialRecord record = financialRecordRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("Record not found with id: " + id));

        record.setAmount(request.getAmount());
        record.setType(request.getType());
        record.setCategory(request.getCategory());
        record.setDate(request.getDate());
        record.setNotes(request.getNotes());

        financialRecordRepository.save(record);
        return mapToResponse(record);
    }

    // Soft delete record
    public void deleteRecord(Long id) {
        FinancialRecord record = financialRecordRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("Record not found with id: " + id));
        record.setDeleted(true);
        financialRecordRepository.save(record);
    }

    // Get currently logged-in user
    private User getCurrentUser() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Authenticated user not found"));
    }

    // Map entity to response DTO
    public FinancialRecordResponse mapToResponse(FinancialRecord record) {
        return FinancialRecordResponse.builder()
                .id(record.getId())
                .amount(record.getAmount())
                .type(record.getType())
                .category(record.getCategory())
                .date(record.getDate())
                .notes(record.getNotes())
                .createdBy(record.getCreatedBy().getName())
                .createdAt(record.getCreatedAt())
                .updatedAt(record.getUpdatedAt())
                .build();
    }
}