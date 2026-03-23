package com.payment.backend.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.stereotype.Indexed;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.Getter;


@Entity
@Data
@Table(name="otps")
public class Otp {

    @Id
    @GeneratedValue(strategy=GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name="user_id")
    private User user;

    private String otpReference;
    private String otpCode;
    private String receiverAccount;
    private BigDecimal amount;
    private boolean isVerified=false;
    private boolean isUsed=false;
    private LocalDateTime expiresAt;
    private LocalDateTime createdAt=LocalDateTime.now();
    
}
