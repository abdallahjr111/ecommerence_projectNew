package com.example.ecommerence_project.service;

import com.example.ecommerence_project.dto.response.UserResponse;

import java.util.List;

public interface AdminService {

    List<UserResponse> getAllUsers();

    UserResponse getUserById(Long id);

    UserResponse toggleUserEnabled(Long id);

    void deleteUser(Long id);
}
