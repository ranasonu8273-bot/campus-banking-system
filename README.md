# Campus Banking System (Java Swing + JDBC + MySQL)

A student project demonstrating OOP, collections, exception handling, concurrency, and database integration through JDBC.

## Features
- Create customer and savings/current account
- Deposit and withdraw money with validation
- Check balance and view recent transactions
- JDBC with prepared statements and database transactions
- Swing GUI; database work runs through `SwingWorker`
- Abstract `Account` class, `SavingsAccount` and `CurrentAccount` inheritance, `BankOperations` interface
- Custom `BankException`, generic `List<String>` transaction history, and synchronized money operations

## Requirements
- JDK 17+
- Maven 3.8+
- MySQL 8+

## Setup
1. Start MySQL.
2. Run `src/main/resources/schema.sql` using MySQL Workbench or the MySQL CLI.
3. Configure `BANK_DB_URL`, `BANK_DB_USER`, and `BANK_DB_PASSWORD` environment variables as needed. Defaults: `jdbc:mysql://localhost:3306/banking_system`, user `root`, empty password.
4. From the project root, run `mvn clean package` and `mvn exec:java -Dexec.mainClass=com.banking.BankingApp`.

## Database design
- `customers(customer_id PK, full_name, email, phone, created_at)`
- `accounts(account_no PK, customer_id FK, account_type, balance, created_at)`
- `transactions(transaction_id PK, account_no FK, transaction_type, amount, note, created_at)`

One customer can own multiple accounts; each account can have multiple transaction records.

## Educational scope
This is a classroom demo, not production banking software. It does not implement login/authentication, encryption, audit-grade security, interest calculations, or real bank integrations. Do not use real customer data.
