package com.robert.portfolio.orders.domain.model;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Currency;

public record Money(BigDecimal amount, Currency currency) {

    public Money {
        if (amount == null || currency == null) {
            throw new IllegalArgumentException("Money amount and currency are required");
        }
        if (amount.signum() < 0) {
            throw new IllegalArgumentException("Money amount cannot be negative");
        }
        amount = amount.setScale(2, RoundingMode.HALF_UP);
    }

    public Money add(Money other) {
        checkCurrency(other);
        return new Money(amount.add(other.amount), currency);
    }

    public Money multiply(Quantity quantity) {
        return new Money(amount.multiply(BigDecimal.valueOf(quantity.value())), currency);
    }

    private void checkCurrency(Money other) {
        if (!currency.equals(other.currency)) {
            throw new IllegalArgumentException("Money currencies must match");
        }
    }
}
