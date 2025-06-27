package com.example.SMSUtility.repository;

import com.example.SMSUtility.model.SMSHistory;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SMSHistoryRepo extends JpaRepository<SMSHistory, Long> {
}
