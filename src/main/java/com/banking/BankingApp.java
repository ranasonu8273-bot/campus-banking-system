package com.banking;

import javax.swing.*;
import java.awt.*;
import java.math.BigDecimal;
import java.util.List;

/** Desktop GUI for the sample banking system. */
public class BankingApp extends JFrame {
    private final BankService service = new BankService();
    private final JTextField accountField = new JTextField(15);
    private final JTextField amountField = new JTextField(10);
    private final JTextArea output = new JTextArea(12, 48);

    public BankingApp() {
        super("Campus Banking System");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(760, 520);
        setLocationRelativeTo(null);
        output.setEditable(false);
        output.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 13));

        JPanel header = new JPanel(new FlowLayout(FlowLayout.LEFT));
        header.add(new JLabel("Account No:")); header.add(accountField);
        header.add(new JLabel("Amount (₹):")); header.add(amountField);

        JPanel actions = new JPanel(new GridLayout(2, 3, 8, 8));
        JButton balance = new JButton("Check Balance");
        JButton deposit = new JButton("Deposit");
        JButton withdraw = new JButton("Withdraw");
        JButton history = new JButton("Transaction History");
        JButton create = new JButton("Create Account");
        JButton clear = new JButton("Clear");
        actions.add(balance); actions.add(deposit); actions.add(withdraw);
        actions.add(history); actions.add(create); actions.add(clear);

        balance.addActionListener(e -> runTask(() -> {
            long account = readAccount();
            show("Current balance: ₹" + service.getBalance(account));
        }));
        deposit.addActionListener(e -> runTask(() -> {
            service.deposit(readAccount(), readAmount());
            show("Deposit successful.");
        }));
        withdraw.addActionListener(e -> runTask(() -> {
            service.withdraw(readAccount(), readAmount());
            show("Withdrawal successful.");
        }));
        history.addActionListener(e -> runTask(() -> {
            List<String> rows = service.getTransactions(readAccount());
            show(rows.isEmpty() ? "No transactions found." : String.join("\n", rows));
        }));
        create.addActionListener(e -> createAccount());
        clear.addActionListener(e -> output.setText(""));

        JPanel content = new JPanel(new BorderLayout(10, 10));
        content.setBorder(BorderFactory.createEmptyBorder(14, 14, 14, 14));
        JLabel title = new JLabel("CAMPUS BANKING SYSTEM", SwingConstants.CENTER);
        title.setFont(new Font("SansSerif", Font.BOLD, 22));
        content.add(title, BorderLayout.NORTH);
        JPanel center = new JPanel(new BorderLayout(8, 8));
        center.add(header, BorderLayout.NORTH); center.add(actions, BorderLayout.CENTER);
        content.add(center, BorderLayout.CENTER);
        content.add(new JScrollPane(output), BorderLayout.SOUTH);
        setContentPane(content);
    }

    private long readAccount() {
        return Long.parseLong(accountField.getText().trim());
    }
    private BigDecimal readAmount() {
        return new BigDecimal(amountField.getText().trim());
    }
    private void show(String message) { output.setText(message); }

    /** Runs database work off the Swing event thread to keep the UI responsive. */
    private void runTask(CheckedTask task) {
        output.setText("Processing...");
        new SwingWorker<String, Void>() {
            @Override protected String doInBackground() throws Exception {
                task.run();
                return "Done";
            }
            @Override protected void done() {
                try { get(); }
                catch (Exception ex) {
                    Throwable cause = ex.getCause() == null ? ex : ex.getCause();
                    JOptionPane.showMessageDialog(BankingApp.this, cause.getMessage(),
                            "Operation failed", JOptionPane.ERROR_MESSAGE);
                    output.setText("Operation failed: " + cause.getMessage());
                }
            }
        }.execute();
    }

    private void createAccount() {
        JTextField name = new JTextField(), email = new JTextField(), phone = new JTextField();
        JComboBox<String> type = new JComboBox<>(new String[]{"SAVINGS", "CURRENT"});
        JTextField opening = new JTextField("0.00");
        JPanel panel = new JPanel(new GridLayout(0, 2, 6, 6));
        panel.add(new JLabel("Full name")); panel.add(name);
        panel.add(new JLabel("Email")); panel.add(email);
        panel.add(new JLabel("Phone")); panel.add(phone);
        panel.add(new JLabel("Account type")); panel.add(type);
        panel.add(new JLabel("Opening balance")); panel.add(opening);
        if (JOptionPane.showConfirmDialog(this, panel, "Create Customer Account",
                JOptionPane.OK_CANCEL_OPTION) != JOptionPane.OK_OPTION) return;
        try {
            long account = service.createCustomer(name.getText(), email.getText(), phone.getText(),
                    (String) type.getSelectedItem(), new BigDecimal(opening.getText().trim()));
            accountField.setText(Long.toString(account));
            show("Account created successfully. Account number: " + account);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Could not create account",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    @FunctionalInterface private interface CheckedTask { void run() throws Exception; }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new BankingApp().setVisible(true));
    }
}
