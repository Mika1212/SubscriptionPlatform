package org.mika1212.cache.controller;

import org.mika1212.cache.entity.InvoiceView;
import org.mika1212.cache.entity.UserCacheDto;
import org.mika1212.cache.entity.UserCacheResponse;
import org.mika1212.cache.exception.BadRequestException;
import org.mika1212.cache.service.UserCacheService;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/users")
public class UserCacheController {

    private final UserCacheService service;

    public UserCacheController(UserCacheService service) {
        this.service = service;
    }

    @GetMapping("/{userId}")
    public UserCacheResponse getUser(
            @PathVariable UUID userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {

        if (page < 0) {
            throw new BadRequestException("page must be >= 0");
        }

        if (size <= 0 || size > 100) {
            throw new BadRequestException("size must be between 1 and 100");
        }

        UserCacheDto cache = service.getUserCache(userId);

        List<InvoiceView> invoices = cache.getInvoices();

        int from = Math.min(page * size, invoices.size());
        int to = Math.min(from + size, invoices.size());

        return new UserCacheResponse(
                cache.getSubscriptions(),
                invoices.subList(from, to),
                page,
                size,
                invoices.size()
        );
    }
}
