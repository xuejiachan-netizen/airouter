package com.xuanjia.airouter.service;

import java.math.BigDecimal;

public interface BillingService {
    BigDecimal calculateBilling(Long modelId, int completionTokens, int promptTokens);
}
