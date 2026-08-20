package com.auditplatform.finance.domain;

import java.math.BigDecimal;
import java.math.RoundingMode;

public final class Money {

    public static final int SCALE = 2;

    private Money() {
    }

    public static BigDecimal scale(BigDecimal value) {
        return value.setScale(SCALE, RoundingMode.HALF_UP);
    }

    public static BigDecimal lineAmount(BigDecimal quantity, BigDecimal unitAmount) {
        return scale(quantity.multiply(unitAmount));
    }
}
