package com.paylink.smsforwarder;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Pulls trxID / amount / sender number out of a bKash or Nagad "money
 * received" SMS. Returns null for anything that isn't clearly a
 * received-money confirmation (sent-money, cash-out, promo SMS, etc.
 * are all ignored on purpose).
 */
public class SmsParser {

    private static final Pattern AMOUNT_PATTERN =
            Pattern.compile("(?:Tk|TK|Taka)\\.?\\s*([0-9][0-9,]*\\.?[0-9]*)");

    private static final Pattern TRXID_PATTERN =
            Pattern.compile("Tr[xn]?\\s*ID[:\\s]*([A-Za-z0-9]{6,15})", Pattern.CASE_INSENSITIVE);

    private static final Pattern FALLBACK_ID_PATTERN =
            Pattern.compile("\\bID[:\\s]*([A-Za-z0-9]{6,15})", Pattern.CASE_INSENSITIVE);

    private static final Pattern SENDER_PATTERN =
            Pattern.compile("from\\s+(01[0-9]{9})", Pattern.CASE_INSENSITIVE);

    public static ParsedSms parse(String body, String fromAddress) {
        if (body == null) return null;

        String method = detectMethod(body, fromAddress);
        if (method == null) return null;

        // Only forward "money received" confirmations — not sent-money,
        // cash-out, payment, or promotional messages.
        String lower = body.toLowerCase();
        if (!lower.contains("received")) return null;

        String trxId = firstGroup(TRXID_PATTERN, body);
        if (trxId == null) trxId = firstGroup(FALLBACK_ID_PATTERN, body);
        if (trxId == null) return null;

        String amountStr = firstGroup(AMOUNT_PATTERN, body);
        if (amountStr == null) return null;
        double amount;
        try {
            amount = Double.parseDouble(amountStr.replace(",", ""));
        } catch (NumberFormatException e) {
            return null;
        }
        if (amount <= 0) return null;

        String sender = firstGroup(SENDER_PATTERN, body);

        return new ParsedSms(method, trxId.toUpperCase(), amount, sender, body);
    }

    private static String detectMethod(String body, String fromAddress) {
        String haystack = (safe(body) + " " + safe(fromAddress)).toLowerCase();
        if (haystack.contains("bkash")) return "bkash";
        if (haystack.contains("nagad")) return "nagad";
        return null;
    }

    private static String safe(String s) {
        return s == null ? "" : s;
    }

    private static String firstGroup(Pattern pattern, String text) {
        Matcher m = pattern.matcher(text);
        if (m.find()) return m.group(1);
        return null;
    }
}
