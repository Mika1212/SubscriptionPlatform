package org.mika1212.common.executor;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.*;

@Configuration
public class BillingExecutorConfig {

    @Bean
    public Executor billingExecutor() {
        return new ThreadPoolExecutor(
                4, 8,
                60L, TimeUnit.SECONDS,
                new LinkedBlockingQueue<>(200)
        );
    }
}
