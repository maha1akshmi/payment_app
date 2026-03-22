package com.payment.backend.repository;

import java.util.Optional;

import com.payment.backend.entity.Otp;

public interface OtpRepository extends JpaRepository<Otp,Long> {
    
    Optional<Otp> findByOtpReference(String otpReference);
}
