package com.example.SMSUtility.config;

import com.example.SMSUtility.model.SMSRequest;
import com.example.SMSUtility.repository.SMSRepo;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.NonTransientResourceException;
import org.springframework.batch.item.ParseException;
import org.springframework.batch.item.UnexpectedInputException;

import java.util.Iterator;
import java.util.List;

@Slf4j
public class ItemReaderImpl implements ItemReader<SMSRequest> {

    private static final Logger logger = LoggerFactory.getLogger(ItemReaderImpl.class);

    private final SMSRepo smsRepo;
    private Iterator<SMSRequest>iterator;

    public ItemReaderImpl(SMSRepo smsRepo) {
        this.smsRepo = smsRepo;
    }

    @Override
    public SMSRequest read() throws Exception, UnexpectedInputException, ParseException, NonTransientResourceException {
        logger.info("Inside the read method");
        if(iterator == null || !iterator.hasNext()){
            //02 In progress
            //03 processing failed
            //04 processed
            List<SMSRequest> batch = smsRepo.fetchPendingRowsInBatch();

            if (batch.isEmpty()) {
                logger.info("No records found to process.");
                return null;
            } else {
                logger.info("Fetched {} records for processing.", batch.size());
            }

            for(SMSRequest sms : batch) {
                sms.setStatus("02");
                sms.setComments("In Progress");
                if(sms.getInsertionOrderId() == 120){
                    throw new IllegalAccessException("40 insertionOrderid");
                }
            }
            smsRepo.saveAll(batch);
            iterator = batch.iterator();
        }
        return iterator.hasNext() ? iterator.next() : null;
    }
}
