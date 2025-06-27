package com.example.SMSUtility.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "NGSMSQUEUE")
public class SMSRequest {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long insertionOrderId;

    private String body;
    private String recipient;
    private String senderID;
    private int retryCount;
    private String status;
    private String module;
    private LocalDate entryDate;
    private String comments;
}
