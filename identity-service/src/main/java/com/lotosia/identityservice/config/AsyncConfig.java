package com.lotosia.identityservice.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;

@Configuration
@EnableAsync
public class AsyncConfig {

    @Bean(name = "emailExecutor")
    public Executor emailExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();

        // Increased capacity for better throughput
        executor.setCorePoolSize(3);
        executor.setMaxPoolSize(10);
        executor.setQueueCapacity(500);
        executor.setThreadNamePrefix("EmailAsync-");
        executor.setKeepAliveSeconds(60);

        // Better rejection handling - log and potentially store for retry
        executor.setRejectedExecutionHandler(
            (runnable, threadPoolExecutor) -> {
                System.err.println("Email task rejected - implementing retry logic or queue persistence");
                // TODO: Implement retry mechanism or queue to database
            }
        );

        executor.setWaitForTasksToCompleteOnShutdown(true);
        executor.setAwaitTerminationSeconds(60); // Increased shutdown timeout

        executor.initialize();
        return executor;
    }
}
