package com.gaspoll.gachanokos;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.provider.Settings;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.ScrollView;
import android.widget.Toast;

public class MainActivity extends Activity {
    private EditText inputNumbers;
    private SharedPreferences prefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        prefs = getSharedPreferences("GachaPrefs", Context.MODE_PRIVATE);
        
        ScrollView scrollView = new ScrollView(this);
        scrollView.setBackgroundColor(Color.parseColor("#0F2027"));
        scrollView.setFillViewport(true);

        LinearLayout mainLayout = new LinearLayout(this);
        mainLayout.setOrientation(LinearLayout.VERTICAL);
        mainLayout.setPadding(48, 64, 48, 64);
        scrollView.addView(mainLayout);

        TextView titleText = new TextView(this);
        titleText.setText("GASPOLL AUTO GACHA PRO");
        titleText.setTextColor(Color.WHITE);
        titleText.setTextSize(24);
        titleText.setTypeface(Typeface.DEFAULT_BOLD);
        titleText.setGravity(Gravity.CENTER);
        titleText.setPadding(0, 0, 0, 32);
        mainLayout.addView(titleText);

        inputNumbers = new EditText(this);
        inputNumbers.setHint("Masukkan list nomor WhatsApp\nFormat bebas (dengan/tanpa +)\nContoh:\n+51930617118\n51929037101");
        inputNumbers.setHintTextColor(Color.GRAY);
        inputNumbers.setTextColor(Color.WHITE);
        inputNumbers.setBackgroundColor(Color.parseColor("#1E293B"));
        inputNumbers.setLines(8);
        inputNumbers.setGravity(Gravity.TOP | Gravity.LEFT);
        inputNumbers.setPadding(32, 32, 32, 32);
        
        String savedNumbers = prefs.getString("number_list", "");
        if(!savedNumbers.isEmpty()) inputNumbers.setText(savedNumbers);
        mainLayout.addView(inputNumbers);

        Button btnStart = new Button(this);
        btnStart.setText("MULAI GACHA OTOMATIS");
        btnStart.setTextColor(Color.WHITE);
        btnStart.setBackgroundColor(Color.parseColor("#4CAF50"));
        btnStart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String nums = inputNumbers.getText().toString().trim();
                if(nums.isEmpty()){
                    Toast.makeText(MainActivity.this, "Masukkan minimal 1 nomor!", Toast.LENGTH_SHORT).show();
                    return;
                }
                prefs.edit()
                    .putString("number_list", nums)
                    .putInt("current_index", 0)
                    .putBoolean("is_running", true)
                    .putString("latest_otp", "")
                    .apply();
                
                Intent launchIntent = getPackageManager().getLaunchIntentForPackage("com.whatsapp.w4b");
                if (launchIntent != null) {
                    Toast.makeText(MainActivity.this, "Membuka WhatsApp Business...", Toast.LENGTH_LONG).show();
                    startActivity(launchIntent);
                } else {
                    Toast.makeText(MainActivity.this, "Gagal: WA Business tidak ditemukan", Toast.LENGTH_SHORT).show();
                }
            }
        });
        
        LinearLayout.LayoutParams btnParams = new LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        btnParams.setMargins(0, 32, 0, 32);
        btnStart.setLayoutParams(btnParams);
        mainLayout.addView(btnStart);

        Button btnAcc = new Button(this);
        btnAcc.setText("1. Aksesibilitas (Wajib)");
        btnAcc.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS));
            }
        });
        mainLayout.addView(btnAcc);

        Button btnNotif = new Button(this);
        btnNotif.setText("2. Baca Notifikasi (Wajib)");
        btnNotif.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent("android.settings.ACTION_NOTIFICATION_LISTENER_SETTINGS"));
            }
        });
        mainLayout.addView(btnNotif);

        setContentView(scrollView);
    }
}
