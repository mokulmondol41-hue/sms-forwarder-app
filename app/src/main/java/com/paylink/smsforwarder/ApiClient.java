package com.paylink.smsforwarder;

import android.util.Log;

import org.json.JSONObject;

import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

/** Plain HttpURLConnection POST — no external HTTP library needed. */
public class ApiClient {

    private static final String TAG = "SmsForwarder";

    /** Runs on whatever thread calls it — always call this off the main thread. */
    public static void ingest(ParsedSms sms, long receivedAtMillis) {
        HttpURLConnection conn = null;
        try {
            JSONObject body = new JSONObject();
            body.put("trxId", sms.trxId);
            body.put("amount", sms.amount);
            body.put("method", sms.method);
            if (sms.senderNumber != null) body.put("senderNumber", sms.senderNumber);
            body.put("receivedAt", receivedAtMillis);
            body.put("rawSms", sms.rawSms);

            URL url = new URL(Config.API_BASE_URL + "/api/sms/ingest");
            conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setRequestProperty("Authorization", "Bearer " + Config.API_KEY);
            conn.setConnectTimeout(15000);
            conn.setReadTimeout(15000);
            conn.setDoOutput(true);

            byte[] payload = body.toString().getBytes(StandardCharsets.UTF_8);
            try (OutputStream os = conn.getOutputStream()) {
                os.write(payload);
            }

            int status = conn.getResponseCode();
            boolean ok = status >= 200 && status < 300;
            Log.d(TAG, "ingest " + sms.trxId + " -> HTTP " + status);
            StatusBus.report(sms.trxId, ok, "HTTP " + status);

        } catch (IOException e) {
            Log.e(TAG, "ingest failed for " + sms.trxId, e);
            StatusBus.report(sms.trxId, false, e.getMessage());
        } catch (Exception e) {
            Log.e(TAG, "unexpected error building ingest request", e);
            StatusBus.report(sms.trxId, false, e.getMessage());
        } finally {
            if (conn != null) conn.disconnect();
        }
    }
}
