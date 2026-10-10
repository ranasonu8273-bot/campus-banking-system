package com.banking;

import java.math.BigDecimal;

public class CurrentAccount extends Account {
    public CurrentAccount(long accountNo, int customerId, BigDecimal balance) {
        super(accountNo, customerId, balance);
    }
    @Override public String getAccountType() { return "CURRENT"; }
}
