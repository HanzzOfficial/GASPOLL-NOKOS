package com.gaspoll.gachanokos;

import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification;
import android.app.Notification;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TelegramNotificationService extends NotificationListenerService {
    @Override
    public void onNotificationPosted(StatusBarNotification sbn) {
        String packageName = sbn.getPackageName();
        if (packageName.contains("org.telegram.messenger") || packageName.contains("org.thunderdog.challegram") || packageName.contains("org.telegram.plus")) {
            Bundle extras = sbn.getNotification().extras;
            CharSequence textChar = extras.getCharSequence(Notification.EXTRA_TEXT);
            if (textChar != null) {
                prosesPesan(textChar.toString());
            }
        }
    }

    private void prosesPesan(String pesan) {
        // Support regex untuk screenshot bot All Tech OTP
        Pattern p = Pattern.compile("(?s)\\+(\\d{1,3}).*?(\\d{4})\\b.*?(?:LIVE|Prefix).*?(\\d{6})");
        Matcher m = p.matcher(pesan);
        if (m.find()) {
            String otp = m.group(3);
            SharedPreferences prefs = getSharedPreferences("GachaPrefs", Context.MODE_PRIVATE);
            prefs.edit().putString("latest_otp", otp).apply();
            Log.d("GACHA_PRO", "OTP Tertangkap: " + otp);
        }
    }
    @Override
    public void onNotificationRemoved(StatusBarNotification sbn) {}
}
