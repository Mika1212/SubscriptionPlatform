package org.mika1212.subscription.properties;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "subscription.prices")
public class SubscriptionPriceProperties {
    private BigDecimal basic;
    private BigDecimal pro;
}
