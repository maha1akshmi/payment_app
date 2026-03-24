package com.payment.backend.controller;

import java.math.BigDecimal;
import java.util.*;

import com.payment.backend.entity.Transaction;
import com.payment.backend.entity.User;
import com.payment.backend.repository.UserRepository;
import com.payment.backend.util.JwtUtil;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import com.payment.backend.entity.User;
import com.payment.backend.repository.TransactionRepository;
import com.payment.backend.repository.UserRepository;
import com.payment.backend.service.TransferService;
import com.payment.backend.util.JwtUtil;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "*")
public class TransferController {

    @Autowired
    private TransferService transferService;
    @Autowired
    private JwtUtil jwtUtil;
    @Autowired
    private UserRepository userRepo;
    @Autowired
    private TransactionRepository txnRepo;

    private User getUser(String header) {
        String email = jwtUtil.extractEmail(header.substring(7));
        return userRepo.findByEmail(email).orElseThrow();
    }

    @PostMapping("/otp/generate")
    public Map generate(@RequestHeader("Authorization") String auth,
                        @RequestBody Map<String, String> body) {
        User sender = getUser(auth);
        return transferService.generateOtp(sender, body.get("receiverAccount"),
                new BigDecimal(body.get("amount")));
    }

    @PostMapping("/otp/verify")
    public Map verify(@RequestHeader("Authorization") String auth,
                      @RequestBody Map<String, String> body) {
        return transferService.verifyOtp(body.get("otpReference"), body.get("otp"));
    }

    @PostMapping("/transfer/send")
    public Map send(@RequestHeader("Authorization") String auth,
                    @RequestBody Map<String, String> body) {
        User sender = getUser(auth);
        return transferService.sendMoney(sender,
                body.get("receiverAccount"),
                new BigDecimal(body.get("amount")),
                body.get("tpin"), body.get("otpReference"));
    }

    @GetMapping("/transaction/history")
    public List<Map<String, Object>> history(@RequestHeader("Authorization") String auth,
                                             @RequestParam(required = false) String type) {
        User user = getUser(auth);
        List<Transaction> transactions;
        if ("SENT".equals(type)) {
            transactions = txnRepo.findBySenderOrderByTimestampDesc(user);
        } else if ("RECEIVED".equals(type)) {
            transactions = txnRepo.findByReceiverOrderByTimestampDesc(user);
        } else {
            transactions = txnRepo.findBySenderOrReceiverOrderByTimestampDesc(user, user);
        }

        List<Map<String, Object>> result = new ArrayList<>();
        for (Transaction txn : transactions) {
            Map<String, Object> map = new LinkedHashMap<>();
            map.put("referenceId", txn.getReferenceId());
            boolean isSender = txn.getSender().getId().equals(user.getId());
            map.put("type", isSender ? "SENT" : "RECEIVED");
            map.put("amount", txn.getAmount());
            map.put("counterpartyName", isSender
                    ? txn.getReceiver().getName()
                    : txn.getSender().getName());
            map.put("status", txn.getStatus());
            map.put("timestamp", txn.getTimestamp());
            result.add(map);
        }
        return result;
    }
}
