package com.payment.backend.service;

import com.payment.backend.entity.*;
import com.payment.backend.repository.*;
import com.payment.backend.util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

@Service
public class AuthService {

    @Autowired private UserRepository userRepo;
    @Autowired private AccountRepository accountRepo;
    @Autowired private JwtUtil jwtUtil;
    private BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

    public Map register(String name, String email, String password, String tpin) {
        User user = new User();
        user.setName(name);
        user.setEmail(email);
        user.setPasswordHash(encoder.encode(password));
        user.setTpinHash(encoder.encode(tpin));
        userRepo.save(user);

        Account account = new Account();
        account.setUser(user);
        account.setAccountNumber(generateAccountNumber());
        accountRepo.save(account);

        Map res = new HashMap();
        res.put("message", "Registration successful");
        res.put("accountNumber", account.getAccountNumber());
        res.put("balance", account.getBalance());
        return res;
    }

    public Map login(String email, String password) {
        User user = userRepo.findByEmail(email).orElseThrow();
        if (!encoder.matches(password, user.getPasswordHash()))
            throw new RuntimeException("Invalid credentials");
        String token = jwtUtil.generateToken(email);
        Account account = accountRepo.findByUser(user).orElseThrow();
        Map res = new HashMap();
        res.put("token", token);
        res.put("name", user.getName());
        res.put("accountNumber", account.getAccountNumber());
        return res;
    }

    private String generateAccountNumber() {
        return String.valueOf(1000000000L + new Random().nextInt(900000000));
    }
}