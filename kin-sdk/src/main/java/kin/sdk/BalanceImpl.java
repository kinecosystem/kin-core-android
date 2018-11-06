package kin.sdk;

import java.math.BigDecimal;

final class BalanceImpl implements Balance {

    private BigDecimal valueInKin;

    BalanceImpl(BigDecimal valueInKin) {
        this.valueInKin = valueInKin;
    }

    @Override
    public BigDecimal value() {
        return valueInKin;
    }

    @Override
    public String value(int precision) {
        return valueInKin.setScale(precision, BigDecimal.ROUND_FLOOR).toString();
    }
}
