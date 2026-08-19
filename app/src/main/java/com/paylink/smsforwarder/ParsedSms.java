package com.paylink.smsforwarder;

/** A bKash/Nagad "money received" SMS, boiled down to what the API needs. */
public class ParsedSms {
    public final String method;       // "bkash" | "nagad"
    public final String trxId;
    public final double amount;
    public final String senderNumber; // may be null
    public final String rawSms;

    public ParsedSms(String method, String trxId, double amount, String senderNumber, String rawSms) {
        this.method = method;
        this.trxId = trxId;
        this.amount = amount;
        this.senderNumber = senderNumber;
        this.rawSms = rawSms;
    }
}
