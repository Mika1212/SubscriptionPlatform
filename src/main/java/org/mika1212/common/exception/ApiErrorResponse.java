package org.mika1212.common.exception;

import java.time.Instant;

public record ApiErrorResponse(
        String message,
        String errorCode,
        int status,
        Instant timestamp
) {}
