package org.mika1212.config;

import org.springframework.amqp.core.Queue;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitConfig {

    public static final String INVOICE_QUEUE = "invoice.queue";

    @Bean
    public Queue invoiceQueue() {
        return new Queue(INVOICE_QUEUE, true);
    }
}
