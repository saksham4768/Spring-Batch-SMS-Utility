# Spring Batch SMS Utility

A Spring Boot application that processes and sends SMS messages using Spring Batch with scheduled, multi-threaded job execution. It reads pending SMS requests from the database, sends them via an external SMS API, logs the results into a history table, and updates the original request with status and retry count.

---

## 🔧 Features

- ✅ Scheduled job execution using `@Scheduled`
- ✅ Spring Batch job with `ItemReader`, `AsyncItemProcessor`, and `AsyncItemWriter`
- ✅ Multithreaded SMS processing using a custom `TaskExecutor`
- ✅ Retry mechanism and failure handling
- ✅ Status updates and history logging
- ✅ PostgreSQL database support (can be configured for others)

---

## 🛠️ Tech Stack

- Java 17+
- Spring Boot
- Spring Batch
- Spring Scheduler
- JPA / Hibernate
- PostgreSQL (or any supported DB)
- Lombok
- SLF4J Logging

---

## 🧠 How It Works

This application uses **Spring Batch** along with **Spring Scheduler** and **multithreaded processing** to send SMS messages reliably and efficiently.

---

### 🔁 1. Scheduled Job Execution

- The batch job is triggered every **6 seconds** using the `@Scheduled(fixedRate = 6000)` annotation.
- A unique timestamp is passed as a job parameter to ensure each run is treated as a new instance.

```java

---
@Scheduled(fixedRate = 6000)
public void scheduler() {
    JobParameters jobParameters = new JobParametersBuilder()
        .addLong("timestamp", System.currentTimeMillis())
        .toJobParameters();
    jobLauncher.run(job, jobParameters);
}

```
---

## 🔄 Batch Job Flow

This project follows a classic **Spring Batch** flow using asynchronous (multi-threaded) processing. Here's how each component works together:


### 📥 1. ItemReader — Fetches Pending SMS

- Reads **10 records** (chunk size) at a time from the `NGSMSQUEUE` table.
- Filters only:
  - SMS with `status IS NULL` or `status = '03'` (retry),
  - and `retry_count < 5`.
- Marks selected rows as `IN_PROGRESS` to prevent double processing.
- Returns the list of records to the processor.

---

### ⚙️ 2. AsyncItemProcessor — Parallel SMS API Call

- Each record is processed **asynchronously using a thread pool**.
- For each SMS:
  - A **POST API call** is made to the SMS gateway.
  - The HTTP response is parsed.
  - Based on the response:
    - If `200 OK`: Status set to `SUCCESS`.
    - Otherwise: Status set to `FAILED`, and retry count is incremented.

---

### 💾 3. AsyncItemWriter — Writes Updates to Database

- Writes are also handled asynchronously.
- For each processed record:
  - A success entry is logged into the `NGSMQUEUE_HISTORY` table.
  - The original `NGSMSQUEUE` record is updated with:
    - Final `status`
    - Retry `comment`
    - Incremented `retry_count`

---

### 🧵 Thread Pool Configuration

You can configure the number of threads in your Spring configuration:

```java
@Bean
public TaskExecutor taskExecutor() {
    ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
    executor.setCorePoolSize(10);
    executor.setMaxPoolSize(20);
    executor.setQueueCapacity(100);
    executor.setThreadNamePrefix("sms-thread-");
    executor.initialize();
    return executor;
}
```
----

###  🚀 High Throughput (Thousands of SMS/hour)
```Java
100 records processed every 6 seconds (10 chunks * 10 threads)
≈ 1000 records per minute
≈ 60,000 records per hour
```

