package com.payment.backend.entity;

import java.time.LocalDateTime;

@Entity
@Table(name="transactions")
public class Transaction{

    @Id
    @GeneratedValue(strategy=GenerationType.IDENTITY)
    private Long id;

    private String referenceId;

    @ManyToOne
    @JoinColumn(name="sender_id")
    private User sender;

    @ManyToOne
    @JoinColumn(name="receiver_id")
    private User receiver;

    private BigDecimal amount;

    private String status="SUCCESS";

    private LocalDateTime timestamp=LocalDateTime.now();


}