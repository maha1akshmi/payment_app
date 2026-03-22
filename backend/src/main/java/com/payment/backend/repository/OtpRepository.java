package com.payment.backend.repository;

import java.util.Optional;

import com.payment.backend.entity.Otp;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OtpRepository extends JpaRepository<Otp,Long> {
    
    Optional<Otp> findByOtpReference(String otpReference);

}
