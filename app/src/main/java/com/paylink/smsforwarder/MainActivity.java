package com.paylink.smsforwarder;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.app.Activity;
import android.text.format.DateFormat;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends Activity implements StatusBus.Listener {

    private static final int REQ_PERMISSIONS = 100;

    private Button btnToggle;
    private TextView tvState;
    private TextView tvLast;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        btnToggle = findViewById(R.id.btnToggle);
        tvState = findViewById(R.id.tvState);
        tvLast = findViewById(R.id.tvLast);

        btnToggle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (Config.isEnabled(MainActivity.this)) {
                    stopForwarding();
                } else {
                    requestPermissionsThenStart();
                }
            }
        });

        refreshUi();
    }

    @Override
    protected void onResume() {
        super.onResume();
        StatusBus.setListener(this);
        refreshUi();
    }

    @Override
    protected void onPause() {
        super.onPause();
        StatusBus.setListener(null);
    }

    private void requestPermissionsThenStart() {
        List<String> needed = new ArrayList<>();
        if (checkSelfPermission(Manifest.permission.RECEIVE_SMS) != PackageManager.PERMISSION_GRANTED) {
            needed.add(Manifest.permission.RECEIVE_SMS);
        }
        if (Build.VERSION.SDK_INT >= 33
                && checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            needed.add(Manifest.permission.POST_NOTIFICATIONS);
        }

        if (needed.isEmpty()) {
            startForwarding();
        } else {
            requestPermissions(needed.toArray(new String[0]), REQ_PERMISSIONS);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode != REQ_PERMISSIONS) return;

        boolean allGranted = true;
        for (int result : grantResults) {
            if (result != PackageManager.PERMISSION_GRANTED) {
                allGranted = false;
                break;
            }
        }

        if (allGranted && grantResults.length > 0) {
            startForwarding();
        } else {
            Toast.makeText(this, "SMS পড়ার অনুমতি ছাড়া অ্যাপটি কাজ করবে না।", Toast.LENGTH_LONG).show();
        }
    }

    private void startForwarding() {
        Config.setEnabled(this, true);
        Intent serviceIntent = new Intent(this, SmsForegroundService.class);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(serviceIntent);
        } else {
            startService(serviceIntent);
        }
        refreshUi();
        Toast.makeText(this, "SMS ফরওয়ার্ডার চালু হয়েছে", Toast.LENGTH_SHORT).show();
    }

    private void stopForwarding() {
        Config.setEnabled(this, false);
        stopService(new Intent(this, SmsForegroundService.class));
        refreshUi();
        Toast.makeText(this, "SMS ফরওয়ার্ডার বন্ধ হয়েছে", Toast.LENGTH_SHORT).show();
    }

    private void refreshUi() {
        boolean enabled = Config.isEnabled(this);
        btnToggle.setText(enabled ? "STOP" : "START");
        tvState.setText(enabled
                ? "অবস্থা: চলছে ✅ — bKash/Nagad SMS এলেই স্বয়ংক্রিয়ভাবে পাঠানো হবে"
                : "অবস্থা: বন্ধ আছে");
    }

    @Override
    public void onReport(String trxId, boolean success, String detail) {
        String time = DateFormat.format("hh:mm:ss a", System.currentTimeMillis()).toString();
        tvLast.setText("সর্বশেষ (" + time + "): TrxID " + trxId + " — "
                + (success ? "পাঠানো হয়েছে ✅" : "ব্যর্থ ❌ (" + detail + ")"));
    }
}
