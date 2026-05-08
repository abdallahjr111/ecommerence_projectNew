package com.example.ecommerence_project.service;

import com.example.ecommerence_project.dto.request.CategoryRequest;
import com.example.ecommerence_project.dto.response.CategoryResponse;

import java.util.List;

public interface CategoryService {

    CategoryResponse create(CategoryRequest request);

    List<CategoryResponse> getAll();

    List<CategoryResponse> getAllActive();

    CategoryResponse getById(Long id);

    CategoryResponse update(Long id, CategoryRequest request);

    void delete(Long id);
}
