package com.payment.backend.controller;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import com.payment.backend.entity.User;
import com.payment.backend.repository.UserRepository;
import com.payment.backend.util.JwtUtil;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;

import com.payment.backend.repository.TransactionRepository;
import com.payment.backend.service.TransferService;

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
  public Map generate(@RequestHeader("Authorization") String auth,@RequestBody Map<String,String> body){
    User sender=getUser(auth);
    return transferService.generateOtp(sender,body.get("receiverAccount"),
    new BigDecimal(body.get("amount")));
  }


  @PostMapping("/otp/verify")
  public Map verify(@RequestHeader("Authorization") String auth,@RequestBody Map<String,String> body){
    return transferService.verifyOtp(body.get("otpReference"), body.get("otp"));
  }

  
  @PostMapping("/transfer/send")
  public Map send(@RequestHeader("Authorization") String auth,
      @RequestBody Map<String,String> body) {
    User sender = getUser(auth);
    return transferService.sendMoney(sender,
      body.get("receiverAccount"),
      new BigDecimal(body.get("amount")),
      body.get("tpin"), body.get("otpReference"));
  }

  @GetMapping("/transaction/history")
  public List history(@RequestHeader("Authorization") String auth,
      @RequestParam(required = false) String type) {
    User user = getUser(auth);
    if ("SENT".equals(type)) return txnRepo.findBySenderOrderByTimestampDesc(user);
    if ("RECEIVED".equals(type)) return txnRepo.findByReceiverOrderByTimestampDesc(user);
    return txnRepo.findBySenderOrReceiverOrderByTimestampDesc(user, user);
  }
}
