package com.example.SMSUtility.config;

import com.example.SMSUtility.model.SMSHistory;
import com.example.SMSUtility.model.SMSRequest;
import com.example.SMSUtility.repository.SMSHistoryRepo;
import com.example.SMSUtility.repository.SMSRepo;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Slf4j
public class ItemProcessorImpl implements ItemProcessor<SMSRequest, SMSRequest> {

    private static final Logger logger = LoggerFactory.getLogger(ItemProcessorImpl.class);

    private final RestTemplate restTemplate;
    private static final String api_URL = "http://localhost:9090/api/v1/message";

    public ItemProcessorImpl(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    @Override
    public SMSRequest process(SMSRequest item) throws Exception {
        try{
            logger.info("Thread:- {} for {}", Thread.currentThread().getName(), item);


            //Validate Input
            if(item.getBody() == null || item.getRecipient() == null){
                failed(item, "Message body and recipient id required");
                return item;
            }

            //Create request body
            Map<String, Object> requestBody= new HashMap<>();
            requestBody.put("recipient", item.getRecipient());
            requestBody.put("smsBody", item.getBody());
            requestBody.put("senderId", item.getSenderID());

            //create Http Headers
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<Map<String, Object>> request = new HttpEntity<>(requestBody, headers);

            //Call API
            ResponseEntity<String> response = restTemplate.postForEntity(api_URL, request, String.class);

            String responseJSON = response.getBody();
            boolean isSuccess = response.getStatusCode().is2xxSuccessful();
            log.info("Thread:- {} response is {}", Thread.currentThread().getName(), responseJSON);
            if(isSuccess){
                item.setComments("Message sent successfully");
                item.setStatus("04");
            }
            else {
                failed(item, "Failed");
            }
        }
        catch (Exception e){
            failed(item, e.getMessage());
            log.error("Thread:- {} and Error:- {}", Thread.currentThread().getName(), e.getMessage());
        }
        return item;
    }

    private void failed(SMSRequest item, String comments){
        item.setStatus("03");
        item.setComments(comments);
        item.setRetryCount(item.getRetryCount() + 1);
    }
}
