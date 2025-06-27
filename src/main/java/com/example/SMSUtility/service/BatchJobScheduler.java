package com.example.SMSUtility.service;

import com.example.SMSUtility.repository.SMSRepo;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.JobParametersInvalidException;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.core.repository.JobExecutionAlreadyRunningException;
import org.springframework.batch.core.repository.JobInstanceAlreadyCompleteException;
import org.springframework.batch.core.repository.JobRestartException;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class BatchJobScheduler {

    private static final Logger logger = LoggerFactory.getLogger(BatchJobScheduler.class);
    private final JobLauncher jobLauncher;
    private final Job job;

    private final SMSRepo smsRepo;

    public BatchJobScheduler(JobLauncher jobLauncher, Job job, SMSRepo smsRepo) {
        this.jobLauncher = jobLauncher;
        this.job = job;
        this.smsRepo = smsRepo;
    }

    @Scheduled(fixedRate = 6000)
    public void scheduler() throws JobInstanceAlreadyCompleteException, JobExecutionAlreadyRunningException, JobParametersInvalidException, JobRestartException {

        long count = smsRepo.countPendingRows();
        if (count == 0) {
            logger.info("No pending SMS rows to process. Skipping job.");
            return;
        }

        JobParameters jobParameters = new JobParametersBuilder()
                .addLong("timestamp", System.currentTimeMillis())
                .toJobParameters();
        try {
            logger.info("Starting job with params: {}", jobParameters);
            jobLauncher.run(job, jobParameters);
        } catch (Exception e) {
            logger.error("Job failed to start: {}", e.getMessage());
        }
    }
}
