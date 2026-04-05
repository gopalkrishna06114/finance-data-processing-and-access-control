package com.example.finance_data_processing_and_access_control.repository;

import com.example.finance_data_processing_and_access_control.entity.User;
import com.example.finance_data_processing_and_access_control.enums.Role;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByEmail(String email);

    boolean existsByEmail(String email);

    List<User> findByRole(Role role);

    List<User> findByActive(boolean active);
}