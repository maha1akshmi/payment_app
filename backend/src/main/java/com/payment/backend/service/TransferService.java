package com.payment.backend.service;

import com.payment.backend.entity.*;
import com.payment.backend.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;

@Service
public class TransferService {

    @Autowired private UserRepository userRepo;
    @Autowired private AccountRepository accountRepo;
    @Autowired private TransactionRepository txnRepo;
    @Autowired private OtpRepository otpRepo;
    @Autowired private JavaMailSender mailSender;
    private BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

    public Map generateOtp(User sender, String receiverAccount, BigDecimal amount) {
        String code = String.format("%06d", new Random().nextInt(999999));
        String ref = "OTP-" + System.currentTimeMillis();
        Otp otp = new Otp();
        otp.setUser(sender);
        otp.setOtpCode(code);
        otp.setOtpReference(ref);
        otp.setReceiverAccount(receiverAccount);
        otp.setAmount(amount);
        otp.setExpiresAt(LocalDateTime.now().plusMinutes(5));
        otpRepo.save(otp);
        sendEmail(sender.getEmail(), "Your OTP", "Your OTP is: " + code);

        Map<String, Object> res = new LinkedHashMap<>();
        res.put("message", "OTP sent to your email");
        res.put("otpReference", ref);
        res.put("expiresIn", "5 minutes");
        return res;
    }

    public Map verifyOtp(String otpRef, String code) {
        Map<String, Object> res = new LinkedHashMap<>();
        Optional<Otp> optOtp = otpRepo.findByOtpReference(otpRef);
        if (optOtp.isEmpty()) {
            res.put("verified", false);
            res.put("message", "Invalid OTP reference");
            return res;
        }
        Otp otp = optOtp.get();
        if (otp.getExpiresAt().isBefore(LocalDateTime.now())) {
            res.put("verified", false);
            res.put("message", "OTP expired");
            return res;
        }
        if (!otp.getOtpCode().equals(code)) {
            res.put("verified", false);
            res.put("message", "Invalid or expired OTP");
            return res;
        }
        otp.setVerified(true);
        otpRepo.save(otp);
        res.put("verified", true);
        res.put("message", "OTP verified successfully");
        return res;
    }

    @Transactional
    public Map sendMoney(User sender, String receiverAccNo,
                         BigDecimal amount, String tpin, String otpRef) {
        Map<String, Object> res = new LinkedHashMap<>();

        // 1. Check TPIN
        if (!encoder.matches(tpin, sender.getTpinHash())) {
            res.put("message", "Invalid TPIN");
            return res;
        }
        // 2. Check OTP verified and not used
        Otp otp = otpRepo.findByOtpReference(otpRef).orElse(null);
        if (otp == null || !otp.isVerified() || otp.isUsed()) {
            res.put("message", "Invalid OTP reference");
            return res;
        }
        // 3. Debit sender
        Account senderAcc = accountRepo.findByUser(sender).orElseThrow();
        if (senderAcc.getBalance().compareTo(amount) < 0) {
            res.put("message", "Insufficient balance");
            return res;
        }
        senderAcc.setBalance(senderAcc.getBalance().subtract(amount));
        accountRepo.save(senderAcc);
        // 4. Credit receiver
        Account receiverAcc = accountRepo.findByAccountNumber(receiverAccNo).orElse(null);
        if (receiverAcc == null) {
            // Rollback: re-add amount to sender
            senderAcc.setBalance(senderAcc.getBalance().add(amount));
            accountRepo.save(senderAcc);
            res.put("message", "Receiver account not found");
            return res;
        }
        receiverAcc.setBalance(receiverAcc.getBalance().add(amount));
        accountRepo.save(receiverAcc);
        // 5. Save transaction
        LocalDateTime now = LocalDateTime.now();
        Transaction txn = new Transaction();
        txn.setSender(sender);
        txn.setReceiver(receiverAcc.getUser());
        txn.setAmount(amount);
        txn.setReferenceId("TXN" + System.currentTimeMillis());
        txn.setTimestamp(now);
        txnRepo.save(txn);
        // 6. Mark OTP used
        otp.setUsed(true);
        otpRepo.save(otp);

        res.put("message", "Transfer successful");
        res.put("referenceId", txn.getReferenceId());
        res.put("amount", amount);
        res.put("receiverName", receiverAcc.getUser().getName());
        res.put("newBalance", senderAcc.getBalance());
        res.put("timestamp", now.toString());
        return res;
    }

    private void sendEmail(String to, String subject, String text) {
        SimpleMailMessage msg = new SimpleMailMessage();
        msg.setTo(to);
        msg.setSubject(subject);
        msg.setText(text);
        mailSender.send(msg);
    }
}
