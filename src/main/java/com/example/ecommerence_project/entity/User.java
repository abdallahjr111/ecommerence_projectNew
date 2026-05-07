package com.example.ecommerence_project.entity;

import jakarta.persistence.*;

import java.util.Date;

@Entity
@Table(name="users")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;
    @Column(name = "email") private String email;
    @Column(name = "password") private String password;
    @Column(name = "createdAt")private Date createdAt;
}
