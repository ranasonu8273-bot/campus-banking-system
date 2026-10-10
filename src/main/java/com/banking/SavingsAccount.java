package com.banking;

import java.math.BigDecimal;

public class SavingsAccount extends Account {
    public SavingsAccount(long accountNo, int customerId, BigDecimal balance) {
        super(accountNo, customerId, balance);
    }
    @Override public String getAccountType() { return "SAVINGS"; }
}
