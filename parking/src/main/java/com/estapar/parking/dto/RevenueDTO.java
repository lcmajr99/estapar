package com.estapar.parking.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class RevenueDTO {

    private BigDecimal amount;
    private String currency;
    private LocalDateTime timestamp;

    public RevenueDTO(BigDecimal amount, LocalDateTime timestamp) {
        this.amount = amount;
        this.currency = "BRL";
        this.timestamp = timestamp;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public String getCurrency() {
        return currency;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }
}
