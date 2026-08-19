package com.paylink.smsforwarder;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Everything you need to edit is right here — no build config, no
 * environment variables, just plain constants.
 */
public class Config {

    // Your deployed Cloudflare Worker's base URL (no trailing slash).
    public static final String API_BASE_URL = "https://your-worker.workers.dev";

    // Must match CONFIG.API_KEY in the Worker's src/index.js exactly.
    public static final String API_KEY = "5b63732a0ba6258be7d1300226f2deeecfe43cfb6869efbb";

    private static final String PREFS = "sms_forwarder_prefs";
    private static final String KEY_ENABLED = "enabled";

    public static boolean isEnabled(Context context) {
        return prefs(context).getBoolean(KEY_ENABLED, false);
    }

    public static void setEnabled(Context context, boolean enabled) {
        prefs(context).edit().putBoolean(KEY_ENABLED, enabled).apply();
    }

    private static SharedPreferences prefs(Context context) {
        return context.getSharedPreferences(PREFS, Context.MODE_PRIVATE);
    }
}
