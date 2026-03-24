package com.payment.backend.entity;



import lombok.Data;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Data
@Entity
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    @Column(unique = true)
    private String email;

    private String passwordHash;

    private String tpinHash;

    private int tpinAttempts = 0;

    private LocalDateTime transferLockedUntil;

    private LocalDateTime createdAt = LocalDateTime.now();

    // generate Getters and Setters
    // Right click → Generate → Getters and Setters in IntelliJ
}