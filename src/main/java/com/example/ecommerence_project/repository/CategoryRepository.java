package com.example.ecommerence_project.repository;

import com.example.ecommerence_project.entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CategoryRepository extends JpaRepository<Category, Long> {
}
