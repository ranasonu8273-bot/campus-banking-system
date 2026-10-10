package com.banking;

import java.math.BigDecimal;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/** Business layer. Transaction operations use a DB transaction and synchronized entry points. */
public class BankService implements BankOperations {
    @Override
    public synchronized void deposit(long accountNo, BigDecimal amount) throws SQLException, BankException {
        validateAmount(amount);
        try (Connection c = Database.getConnection()) {
            c.setAutoCommit(false);
            try {
                BigDecimal balance = lockAndReadBalance(c, accountNo);
                updateBalance(c, accountNo, balance.add(amount));
                addTransaction(c, accountNo, "DEPOSIT", amount, "Cash deposit");
                c.commit();
            } catch (Exception e) {
                c.rollback();
                if (e instanceof BankException be) throw be;
                if (e instanceof SQLException se) throw se;
                throw new SQLException("Deposit failed", e);
            }
        }
    }

    @Override
    public synchronized void withdraw(long accountNo, BigDecimal amount) throws SQLException, BankException {
        validateAmount(amount);
        try (Connection c = Database.getConnection()) {
            c.setAutoCommit(false);
            try {
                BigDecimal balance = lockAndReadBalance(c, accountNo);
                if (balance.compareTo(amount) < 0) throw new BankException("Insufficient balance.");
                updateBalance(c, accountNo, balance.subtract(amount));
                addTransaction(c, accountNo, "WITHDRAWAL", amount, "Cash withdrawal");
                c.commit();
            } catch (Exception e) {
                c.rollback();
                if (e instanceof BankException be) throw be;
                if (e instanceof SQLException se) throw se;
                throw new SQLException("Withdrawal failed", e);
            }
        }
    }

    @Override
    public BigDecimal getBalance(long accountNo) throws SQLException, BankException {
        String sql = "SELECT balance FROM accounts WHERE account_no = ?";
        try (Connection c = Database.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setLong(1, accountNo);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) throw new BankException("Account not found.");
                return rs.getBigDecimal("balance");
            }
        }
    }

    @Override
    public List<String> getTransactions(long accountNo) throws SQLException {
        List<String> rows = new ArrayList<>();
        String sql = "SELECT transaction_type, amount, note, created_at FROM transactions WHERE account_no=? ORDER BY transaction_id DESC LIMIT 50";
        try (Connection c = Database.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setLong(1, accountNo);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    rows.add(rs.getTimestamp("created_at") + " | " + rs.getString("transaction_type")
                            + " | ₹" + rs.getBigDecimal("amount") + " | " + rs.getString("note"));
                }
            }
        }
        return rows;
    }

    /** Creates a customer and their first account as one atomic operation. */
    public long createCustomer(String name, String email, String phone, String type, BigDecimal openingBalance)
            throws SQLException, BankException {
        if (name == null || name.isBlank() || email == null || !email.contains("@"))
            throw new BankException("Enter a valid name and email.");
        if (openingBalance == null || openingBalance.signum() < 0)
            throw new BankException("Opening balance cannot be negative.");
        long accountNo = 1000000000L + System.currentTimeMillis() % 9000000000L;
        try (Connection c = Database.getConnection()) {
            c.setAutoCommit(false);
            try {
                int customerId;
                try (PreparedStatement ps = c.prepareStatement(
                        "INSERT INTO customers(full_name,email,phone) VALUES(?,?,?)", Statement.RETURN_GENERATED_KEYS)) {
                    ps.setString(1, name.trim()); ps.setString(2, email.trim()); ps.setString(3, phone);
                    ps.executeUpdate();
                    try (ResultSet keys = ps.getGeneratedKeys()) {
                        if (!keys.next()) throw new SQLException("Customer ID was not generated.");
                        customerId = keys.getInt(1);
                    }
                }
                try (PreparedStatement ps = c.prepareStatement(
                        "INSERT INTO accounts(account_no,customer_id,account_type,balance) VALUES(?,?,?,?)")) {
                    ps.setLong(1, accountNo); ps.setInt(2, customerId);
                    ps.setString(3, type); ps.setBigDecimal(4, openingBalance); ps.executeUpdate();
                }
                if (openingBalance.signum() > 0) addTransaction(c, accountNo, "DEPOSIT", openingBalance, "Opening balance");
                c.commit();
                return accountNo;
            } catch (Exception e) {
                c.rollback();
                if (e instanceof SQLException se) throw se;
                if (e instanceof BankException be) throw be;
                throw new SQLException("Customer creation failed", e);
            }
        }
    }

    private BigDecimal lockAndReadBalance(Connection c, long accountNo) throws SQLException, BankException {
        try (PreparedStatement ps = c.prepareStatement(
                "SELECT balance FROM accounts WHERE account_no=? FOR UPDATE")) {
            ps.setLong(1, accountNo);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) throw new BankException("Account not found.");
                return rs.getBigDecimal("balance");
            }
        }
    }

    private void updateBalance(Connection c, long accountNo, BigDecimal balance) throws SQLException {
        try (PreparedStatement ps = c.prepareStatement("UPDATE accounts SET balance=? WHERE account_no=?")) {
            ps.setBigDecimal(1, balance); ps.setLong(2, accountNo); ps.executeUpdate();
        }
    }

    private void addTransaction(Connection c, long accountNo, String type, BigDecimal amount, String note)
            throws SQLException {
        try (PreparedStatement ps = c.prepareStatement(
                "INSERT INTO transactions(account_no,transaction_type,amount,note) VALUES(?,?,?,?)")) {
            ps.setLong(1, accountNo); ps.setString(2, type); ps.setBigDecimal(3, amount);
            ps.setString(4, note); ps.executeUpdate();
        }
    }

    private void validateAmount(BigDecimal amount) throws BankException {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0)
            throw new BankException("Amount must be greater than zero.");
        if (amount.scale() > 2) throw new BankException("Amount can have at most two decimal places.");
    }
}
