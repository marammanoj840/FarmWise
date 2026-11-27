package com.farmwise.backend.marketplace;

import com.farmwise.backend.config.IntegrationConfig.IntegrationProperties;
import java.math.BigDecimal;
import org.springframework.stereotype.Component;

@Component
public class ConfigurablePaymentProvider implements PaymentProvider {

    private final IntegrationProperties properties;

    public ConfigurablePaymentProvider(IntegrationProperties properties) {
        this.properties = properties;
    }

    @Override
    public String providerName() {
        return properties.getPayment().getProvider();
    }

    @Override
    public boolean isConfigured() {
        return properties.getPayment().isConfigured();
    }

    @Override
    public PaymentCreationResult initiateOrderPayment(String orderNumber, BigDecimal amount, String currency) {
        if (!isConfigured()) {
            return new PaymentCreationResult(
                    "unconfigured",
                    "UNCONFIGURED",
                    "PENDING_PAYMENT_CONFIG",
                    "Live payment gateway credentials are not configured. "
                            + "Order " + orderNumber + " has been recorded for offline/invoice follow-up without online card/UPI capture.");
        }
        return new PaymentCreationResult(
                providerName(),
                "PENDING_GATEWAY_CAPTURE",
                "AWAITING_PAYMENT",
                "Payment intent initialized with configured provider " + providerName() + ".");
    }
}
