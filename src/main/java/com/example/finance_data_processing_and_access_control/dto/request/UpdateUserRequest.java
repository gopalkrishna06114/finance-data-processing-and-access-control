package com.example.finance_data_processing_and_access_control.dto.request;

import com.example.finance_data_processing_and_access_control.enums.Role;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UpdateUserRequest {

    @Size(min = 2, max = 100, message = "Name must be between 2 and 100 characters")
    private String name;

    private Role role;

    private Boolean active;
}