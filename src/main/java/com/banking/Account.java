package com.banking;

import java.math.BigDecimal;

/** Abstract parent class demonstrates inheritance and abstraction. */
public abstract class Account {
    protected final long accountNo;
    protected final int customerId;
    protected BigDecimal balance;

    protected Account(long accountNo, int customerId, BigDecimal balance) {
        this.accountNo = accountNo;
        this.customerId = customerId;
        this.balance = balance;
    }

    public long getAccountNo() { return accountNo; }
    public int getCustomerId() { return customerId; }
    public BigDecimal getBalance() { return balance; }
    public abstract String getAccountType();
}
