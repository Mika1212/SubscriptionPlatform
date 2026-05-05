package org.mika1212.cache.entity;

import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

@Getter
public class UserCacheDto {

    private List<SubscriptionView> subscriptions = new ArrayList<>();
    private List<InvoiceView> invoices = new ArrayList<>();

    public UserCacheDto() {}

    public UserCacheDto(List<SubscriptionView> subscriptions,
                        List<InvoiceView> invoices) {
        this.subscriptions = subscriptions;
        this.invoices = invoices;
    }
}
