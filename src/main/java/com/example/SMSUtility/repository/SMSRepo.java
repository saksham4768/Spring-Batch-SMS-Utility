package com.example.SMSUtility.repository;

import com.example.SMSUtility.model.SMSRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface SMSRepo extends JpaRepository<SMSRequest, Long> {

    @Query(value = "select * from NGSMSQUEUE where retry_count != 5 AND (status IS NULL OR status = '03') LIMIT 15", nativeQuery = true)
    List<SMSRequest> fetchPendingRowsInBatch();

    @Query(value = "SELECT COUNT(*) FROM NGSMSQUEUE WHERE retry_count != 5 AND (status IS NULL OR status = '03')", nativeQuery = true)
    long countPendingRows();
}
