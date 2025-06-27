package com.example.SMSUtility.config;

import com.example.SMSUtility.model.SMSHistory;
import com.example.SMSUtility.model.SMSRequest;
import com.example.SMSUtility.repository.SMSHistoryRepo;
import com.example.SMSUtility.repository.SMSRepo;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.item.Chunk;
import org.springframework.batch.item.ItemWriter;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Slf4j
public class ItemWriterImpl implements ItemWriter<SMSRequest> {
    private final SMSRepo smsRepo;

    private final SMSHistoryRepo smsHistoryRepo;

    private final Logger logger = LoggerFactory.getLogger(ItemWriterImpl.class);
    public ItemWriterImpl(SMSRepo smsRepo, SMSHistoryRepo smsHistoryRepo) {
        this.smsRepo = smsRepo;
        this.smsHistoryRepo = smsHistoryRepo;
    }

    @Override
    public void write(Chunk<? extends SMSRequest> chunk) throws Exception {
        try {

            List<SMSHistory> toHistory = new ArrayList<>();
            for(SMSRequest sms : chunk){
                if(sms.getStatus().equalsIgnoreCase("04")){
                    SMSHistory smsHistory = new SMSHistory();
                    smsHistory.setBody(sms.getBody());
                    smsHistory.setRecipient(sms.getRecipient());
                    smsHistory.setSenderId("TajFin-AD");
                    smsHistory.setModule(sms.getModule());
                    smsHistory.setStatus("SUCCESS");
                    smsHistory.setProcessingTime(LocalDateTime.now());
                    smsHistory.setComments("Message sent successfully");
                    smsHistory.setResponseCode("200");
                    //history.setMessageId(extractMessageId(responseJSON)); // optional: parse JSON
                    toHistory.add(smsHistory);
                }
            }
            smsRepo.saveAll(chunk);
            smsHistoryRepo.saveAll(toHistory);
        } catch (Exception e) {
            logger.error("Error in ItemWriter while saving SMSRequests or History: {}", e.getMessage(), e);
            throw new Exception("Failed to write chunk in ItemWriter", e);
        }
    }
}
