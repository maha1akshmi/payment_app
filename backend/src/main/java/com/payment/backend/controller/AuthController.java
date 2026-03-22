package com.payment.backend.controller;

import com.payment.backend.entity.*;
import com.payment.backend.repository.*;
import com.payment.backend.service.AuthService;
import com.payment.backend.util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "*")
public class AuthController {

    @Autowired private AuthService authService;
    @Autowired private JwtUtil jwtUtil;
    @Autowired private UserRepository userRepo;
    @Autowired private AccountRepository accountRepo;

    @PostMapping("/auth/register")
    public Map register(@RequestBody Map<String,String> body) {
        return authService.register(
                body.get("name"), body.get("email"),
                body.get("password"), body.get("tpin"));
    }

    @PostMapping("/auth/login")
    public Map login(@RequestBody Map<String,String> body) {
        return authService.login(body.get("email"), body.get("password"));
    }

    @GetMapping("/account/balance")
    public Map balance(@RequestHeader("Authorization") String authHeader) {
        String email = jwtUtil.extractEmail(authHeader.substring(7));
        User user = userRepo.findByEmail(email).orElseThrow();
        Account acc = accountRepo.findByUser(user).orElseThrow();
        return Map.of("balance", acc.getBalance(),
                "accountNumber", acc.getAccountNumber(), "currency", "INR");
    }

    @GetMapping("/account/profile")
    public Map profile(@RequestHeader("Authorization") String authHeader) {
        String email = jwtUtil.extractEmail(authHeader.substring(7));
        User user = userRepo.findByEmail(email).orElseThrow();
        Account acc = accountRepo.findByUser(user).orElseThrow();
        return Map.of("name", user.getName(), "email", user.getEmail(),
                "accountNumber", acc.getAccountNumber(),"joinedDate",user.getCreatedAt());
    }
}