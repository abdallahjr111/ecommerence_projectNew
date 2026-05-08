package com.example.ecommerence_project.service;

import com.example.ecommerence_project.dto.request.LoginRequest;
import com.example.ecommerence_project.dto.request.RegisterRequest;
import com.example.ecommerence_project.dto.response.AuthResponse;

public interface AuthService {

    AuthResponse register(RegisterRequest request);

    AuthResponse login(LoginRequest request);
}
