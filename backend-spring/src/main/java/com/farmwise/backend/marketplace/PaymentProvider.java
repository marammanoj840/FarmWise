package com.farmwise.backend.marketplace;

import java.math.BigDecimal;

public interface PaymentProvider {
    String providerName();

    boolean isConfigured();

    PaymentCreationResult initiateOrderPayment(String orderNumber, BigDecimal amount, String currency);

    record PaymentCreationResult(
            String provider,
            String paymentStatus,
            String orderStatus,
            String notice) {
    }
}
