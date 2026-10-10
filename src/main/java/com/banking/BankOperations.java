package com.banking;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.List;

public interface BankOperations {
    void deposit(long accountNo, BigDecimal amount) throws SQLException, BankException;
    void withdraw(long accountNo, BigDecimal amount) throws SQLException, BankException;
    BigDecimal getBalance(long accountNo) throws SQLException, BankException;
    List<String> getTransactions(long accountNo) throws SQLException;
}
