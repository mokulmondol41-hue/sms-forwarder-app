package com.paylink.smsforwarder;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.provider.Telephony;
import android.telephony.SmsMessage;
import android.util.Log;

public class SmsReceiver extends BroadcastReceiver {

    private static final String TAG = "SmsForwarder";

    @Override
    public void onReceive(Context context, Intent intent) {
        if (!Config.isEnabled(context)) return; // "Start" hasn't been tapped — ignore everything
        if (intent == null || !Telephony.Sms.Intents.SMS_RECEIVED_ACTION.equals(intent.getAction())) return;

        SmsMessage[] messages = Telephony.Sms.Intents.getMessagesFromIntent(intent);
        if (messages == null) return;

        // A single SMS can arrive split across several PDUs — join them back
        // into one message body before parsing.
        StringBuilder bodyBuilder = new StringBuilder();
        String from = null;
        long timestamp = System.currentTimeMillis();
        for (SmsMessage part : messages) {
            if (part == null) continue;
            bodyBuilder.append(part.getMessageBody());
            if (from == null) from = part.getOriginatingAddress();
            timestamp = part.getTimestampMillis();
        }

        String body = bodyBuilder.toString();
        ParsedSms parsed = SmsParser.parse(body, from);
        if (parsed == null) {
            Log.d(TAG, "Ignored SMS (not a recognized received-money confirmation).");
            return;
        }

        final long ts = timestamp;
        final PendingResult pendingResult = goAsync();
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    ApiClient.ingest(parsed, ts);
                } finally {
                    pendingResult.finish();
                }
            }
        }).start();
    }
}
