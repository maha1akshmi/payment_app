package com.payment.backend.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.stereotype.Indexed;

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
