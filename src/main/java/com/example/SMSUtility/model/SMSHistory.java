package com.example.SMSUtility.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Data
@Table(name = "NGSMSQUEUEHISTORY")
public class SMSHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long insertionOrderId;

    @Column(columnDefinition = "TEXT")
    private String body;

    private String recipient;
    private String senderId;
    private String module;
    private String status;
    private LocalDateTime processingTime;
    private String comments;
    private String responseCode;
    private String messageId;
}

