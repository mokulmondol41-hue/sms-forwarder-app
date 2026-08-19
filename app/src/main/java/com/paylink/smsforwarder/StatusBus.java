package com.paylink.smsforwarder;

import android.os.Handler;
import android.os.Looper;

/**
 * Tiny pub/sub so MainActivity can show "last SMS forwarded" status
 * without needing AndroidX's LocalBroadcastManager or any library.
 */
public class StatusBus {

    public interface Listener {
        void onReport(String trxId, boolean success, String detail);
    }

    private static volatile Listener listener;
    private static final Handler MAIN = new Handler(Looper.getMainLooper());

    public static void setListener(Listener l) {
        listener = l;
    }

    public static void report(final String trxId, final boolean success, final String detail) {
        final Listener l = listener;
        if (l == null) return;
        MAIN.post(new Runnable() {
            @Override
            public void run() {
                l.onReport(trxId, success, detail);
            }
        });
    }
}
