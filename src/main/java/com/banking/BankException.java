package com.banking;

/** Domain-specific exception for invalid banking operations. */
public class BankException extends Exception {
    public BankException(String message) { super(message); }
}
