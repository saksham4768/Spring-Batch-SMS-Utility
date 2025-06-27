package com.example.SMSUtility.config;

import com.example.SMSUtility.model.SMSRequest;
import com.example.SMSUtility.repository.SMSHistoryRepo;
import com.example.SMSUtility.repository.SMSRepo;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.integration.async.AsyncItemProcessor;
import org.springframework.batch.integration.async.AsyncItemWriter;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.ItemWriter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.SimpleAsyncTaskExecutor;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.web.client.RestTemplate;

import java.util.concurrent.Future;

@Configuration
@Slf4j
public class BatchConfig {

    private static final Logger logger = LoggerFactory.getLogger(BatchConfig.class);
    // 1. Reader Bean
    @Bean
    @StepScope
    public ItemReader<SMSRequest> itemReader(SMSRepo smsRepo) {
        logger.info("Inside the itemReader");
        return new ItemReaderImpl(smsRepo);
    }

    // 2. Actual Processor (sync)
    @Bean
    public ItemProcessor<SMSRequest, SMSRequest> actualProcessor(RestTemplate restTemplate) {
        logger.info("Inside the ItemProcessor");
        return new ItemProcessorImpl(restTemplate);
    }

    // 3. Async Processor
    @Bean
    public AsyncItemProcessor<SMSRequest, SMSRequest> asyncItemProcessor(ItemProcessor<SMSRequest, SMSRequest> actualProcessor, ThreadPoolTaskExecutor executor) {
        logger.info("Inside the asyncItemProcessor");
        AsyncItemProcessor<SMSRequest, SMSRequest> asyncProcessor = new AsyncItemProcessor<>();
        asyncProcessor.setDelegate(actualProcessor);
        asyncProcessor.setTaskExecutor(executor);
        return asyncProcessor;
    }

    // 4. Writer Bean
    @Bean
    public ItemWriter<SMSRequest> itemWriter(SMSRepo smsRepo, SMSHistoryRepo smsHistoryRepo) {
        logger.info("Inside the Writer");
        return new ItemWriterImpl(smsRepo,smsHistoryRepo);
    }

    // 5. Async Writer
    @Bean
    public AsyncItemWriter<SMSRequest> asyncItemWriter(ItemWriter<SMSRequest> delegate) {
        logger.info("Inside the asyncItemWriter");
        AsyncItemWriter<SMSRequest> asyncWriter = new AsyncItemWriter<>();
        asyncWriter.setDelegate(delegate);
        return asyncWriter;
    }

    // 6. Step Bean
    @Bean
    public Step taskStep(JobRepository jobRepository,
                         PlatformTransactionManager platformTransactionManager,
                         ItemReader<SMSRequest> itemReader,
                         AsyncItemProcessor<SMSRequest, SMSRequest> processor,
                         AsyncItemWriter<SMSRequest> writer) {
        logger.info("Inside taskStep bean");
        return new StepBuilder("taskStep", jobRepository)
                .<SMSRequest, Future<SMSRequest>>chunk(10, platformTransactionManager)
                .reader(itemReader)
                .processor(processor)
                .writer(writer)
                .build();
    }

    // 7. Job Bean
    @Bean
    public Job job(JobRepository jobRepository, Step taskStep) {
        logger.info("Inside the job bean");
        return new JobBuilder("job", jobRepository)
                .start(taskStep)
                .build();
    }
}
