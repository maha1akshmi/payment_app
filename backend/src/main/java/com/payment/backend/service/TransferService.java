package com.payment.backend.service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.Random;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import com.payment.backend.entity.Account;
import com.payment.backend.entity.Otp;
import com.payment.backend.entity.Transaction;
import com.payment.backend.entity.User;
import com.payment.backend.repository.AccountRepository;
import com.payment.backend.repository.OtpRepository;
import com.payment.backend.repository.TransactionRepository;
import com.payment.backend.repository.UserRepository;

import jakarta.transaction.Transactional;

@Service
public class TransferService {
    
      @Autowired private UserRepository userRepo;
  @Autowired private AccountRepository accountRepo;
  @Autowired private TransactionRepository txnRepo;
  @Autowired private OtpRepository otpRepo;
  @Autowired private JavaMailSender mailSender;


    private BCryptPasswordEncoder encoder=new BCryptPasswordEncoder();

    public Map generateOtp(User sender, String receiverAccount, BigDecimal amount) {
        String code = String.format("%06d", new Random().nextInt(999999));
        String ref = "OTP-" + System.currentTimeMillis();
        Otp otp = new Otp();
        otp.setUser(sender); otp.setOtpCode(code);
        otp.setOtpReference(ref);
        otp.setReceiverAccount(receiverAccount);
        otp.setAmount(amount);
        otp.setExpiresAt(LocalDateTime.now().plusMinutes(5));
        otpRepo.save(otp);
        sendEmail(sender.getEmail(), "Your OTP", "Your OTP is: " + code);
        return Map.of("message","OTP sent","otpReference",ref,"expiresIn","5 minutes");
    }

    public Map verifyOtp(String otpRef, String code) {
        Otp otp = otpRepo.findByOtpReference(otpRef).orElseThrow();
        if (otp.getExpiresAt().isBefore(LocalDateTime.now()))
            return Map.of("verified",false,"message","OTP expired");
        if (!otp.getOtpCode().equals(code))
            return Map.of("verified",false,"message","Wrong OTP");
        otp.setVerified(true); otpRepo.save(otp);
        return Map.of("verified",true,"message","OTP verified");
    }


    @Transactional
    public Map sendMoney(User sender, String receiverAccNo,
                         BigDecimal amount, String tpin, String otpRef) {
        // 1. Check TPIN
        if (!encoder.matches(tpin, sender.getTpinHash()))
            throw new RuntimeException("Invalid TPIN");
        // 2. Check OTP verified and not used
        Otp otp = otpRepo.findByOtpReference(otpRef).orElseThrow();
        if (!otp.isVerified() || otp.isUsed())
            throw new RuntimeException("Invalid OTP reference");
        // 3. Debit sender
        Account senderAcc = accountRepo.findByUser(sender).orElseThrow();
        if (senderAcc.getBalance().compareTo(amount) < 0)
            throw new RuntimeException("Insufficient balance");
        senderAcc.setBalance(senderAcc.getBalance().subtract(amount));
        accountRepo.save(senderAcc);
        // 4. Credit receiver
        Account receiverAcc = accountRepo.findByAccountNumber(receiverAccNo).orElseThrow();
        receiverAcc.setBalance(receiverAcc.getBalance().add(amount));
        accountRepo.save(receiverAcc);
        // 5. Save transaction
        Transaction txn = new Transaction();
        txn.setSender(sender); txn.setReceiver(receiverAcc.getUser());
        txn.setAmount(amount);
        txn.setReferenceId("TXN" + System.currentTimeMillis());
        txnRepo.save(txn);
        // 6. Mark OTP used
        otp.setUsed(true); otpRepo.save(otp);
        return Map.of("message","Transfer successful",
                "referenceId",txn.getReferenceId(),
                "newBalance",senderAcc.getBalance());
    }

    private void sendEmail(String to, String subject, String text) {
        SimpleMailMessage msg = new SimpleMailMessage();
        msg.setTo(to); msg.setSubject(subject); msg.setText(text);
        mailSender.send(msg);
    }
}

