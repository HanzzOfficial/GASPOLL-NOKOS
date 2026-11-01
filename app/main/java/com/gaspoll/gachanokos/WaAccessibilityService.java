package com.gaspoll.gachanokos;

import android.accessibilityservice.AccessibilityService;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;
import android.widget.Toast;
import java.util.ArrayList;
import java.util.List;

public class WaAccessibilityService extends AccessibilityService {
    private String textBanding = "Halo, nomor saya tidak dapat menerima SMS dari WhatsApp. Tolong bantu tinjau kembali karena saya membutuhkannya untuk bisnis.";
    private long lastActionTime = 0;
    private int displayedIndex = -1; 

    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
        if (System.currentTimeMillis() - lastActionTime < 1500) return; 

        SharedPreferences prefs = getSharedPreferences("GachaPrefs", Context.MODE_PRIVATE);
        if (!prefs.getBoolean("is_running", false)) return;

        AccessibilityNodeInfo rootNode = getRootInActiveWindow();
        if (rootNode == null) return;

        String allNumbers = prefs.getString("number_list", "");
        int currentIndex = prefs.getInt("current_index", 0);
        String[] numArray = allNumbers.split("\\n");

        if (currentIndex >= numArray.length) {
            if (displayedIndex != currentIndex) {
                showToast("Semua nomor telah diproses! Sistem Gacha dihentikan.");
                prefs.edit().putBoolean("is_running", false).apply();
                displayedIndex = currentIndex;
            }
            return;
        }

        String rawNumber = numArray[currentIndex].trim().replace("+", "").replace(" ", "");
        if (rawNumber.isEmpty()) {
            lanjutKeNomorBerikutnya(prefs, currentIndex);
            return;
        }
        
        // 1. AUTO INPUT NOMOR
        List<AccessibilityNodeInfo> editTexts = new ArrayList<>();
        findNodeByClassName(rootNode, "android.widget.EditText", editTexts);
        if (!editTexts.isEmpty()) {
            AccessibilityNodeInfo phoneField = editTexts.get(editTexts.size() - 1);
            if (phoneField.getText() == null || !phoneField.getText().toString().replace(" ", "").equals(rawNumber)) {
                Bundle arguments = new Bundle();
                arguments.putCharSequence(AccessibilityNodeInfo.ACTION_ARGUMENT_SET_TEXT_CHARSEQUENCE, rawNumber);
                phoneField.performAction(AccessibilityNodeInfo.ACTION_SET_TEXT, arguments);
                
                if (displayedIndex != currentIndex) {
                    showToast("Proses (" + (currentIndex + 1) + "/" + numArray.length + "): " + rawNumber);
                    displayedIndex = currentIndex;
                }
                
                if (clickNodeByText(rootNode, "Next", "Lanjut")) lastActionTime = System.currentTimeMillis();
            }
        }

        // 2. DETEKSI BANNED & BANDING
        if (hasNodeWithText(rootNode, "banned") || hasNodeWithText(rootNode, "diblokir") || hasNodeWithText(rootNode, "not allowed")) {
            showToast("BANNED! Mengajukan Banding...");
            if (clickNodeByText(rootNode, "Contact us", "Hubungi kami") || clickNodeByText(rootNode, "Support", "Dukungan")) {
                lastActionTime = System.currentTimeMillis();
            }
        }

        List<AccessibilityNodeInfo> inputIssueNodes = new ArrayList<>();
        findNodeByClassName(rootNode, "android.widget.EditText", inputIssueNodes); 
        if (!inputIssueNodes.isEmpty() && (hasNodeWithText(rootNode, "Describe your issue") || hasNodeWithText(rootNode, "Jelaskan masalah"))) {
            AccessibilityNodeInfo issueField = inputIssueNodes.get(0);
            if (issueField.getText() == null || !issueField.getText().toString().equals(textBanding)) {
                 Bundle arguments = new Bundle();
                 arguments.putCharSequence(AccessibilityNodeInfo.ACTION_ARGUMENT_SET_TEXT_CHARSEQUENCE, textBanding);
                 issueField.performAction(AccessibilityNodeInfo.ACTION_SET_TEXT, arguments);
            }
            if (clickNodeByText(rootNode, "Next", "Lanjut") || clickNodeByText(rootNode, "Send", "Kirim")) {
                showToast("Banding Terkirim! Lanjut nomor berikutnya...");
                lanjutKeNomorBerikutnya(prefs, currentIndex);
            }
        }
        
        // 3. DETEKSI HALAMAN OTP (VERIFY)
        if (hasNodeWithText(rootNode, "Verify") || hasNodeWithText(rootNode, "Verifikasi") || hasNodeWithText(rootNode, "SMS")) {
             String otpTersimpan = prefs.getString("latest_otp", "");
             if (!otpTersimpan.isEmpty()) {
                 showToast("OTP Ditemukan: " + otpTersimpan + ". Silakan input/Lanjut...");
                 prefs.edit().putString("latest_otp", "").apply();
                 lanjutKeNomorBerikutnya(prefs, currentIndex);
             }
        }
        
        // 4. KLIK POPUP KONFIRMASI (Nomor Benar / Salah)
        if (hasNodeWithText(rootNode, "Is this the correct number") || hasNodeWithText(rootNode, "Apakah ini nomor yang benar") || hasNodeWithText(rootNode, "not a valid")) {
            if (clickNodeByText(rootNode, "Yes", "Ya") || clickNodeByText(rootNode, "OK", "OKE")) lastActionTime = System.currentTimeMillis();
        }
    }

    private void lanjutKeNomorBerikutnya(SharedPreferences prefs, int currentIndex) {
        prefs.edit().putInt("current_index", currentIndex + 1).apply();
        lastActionTime = System.currentTimeMillis();
    }

    private void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }
    
    private boolean clickNodeByText(AccessibilityNodeInfo root, String textEn, String textId) {
        List<AccessibilityNodeInfo> nodes = root.findAccessibilityNodeInfosByText(textEn);
        if (nodes == null || nodes.isEmpty()) nodes = root.findAccessibilityNodeInfosByText(textId);
        if (nodes != null && !nodes.isEmpty()) {
            for (AccessibilityNodeInfo node : nodes) {
                if (node.isClickable()) { node.performAction(AccessibilityNodeInfo.ACTION_CLICK); return true; } 
                else if (node.getParent() != null && node.getParent().isClickable()) { node.getParent().performAction(AccessibilityNodeInfo.ACTION_CLICK); return true; }
            }
        }
        return false;
    }
    
    private boolean hasNodeWithText(AccessibilityNodeInfo root, String text) {
        List<AccessibilityNodeInfo> nodes = root.findAccessibilityNodeInfosByText(text);
        return (nodes != null && !nodes.isEmpty());
    }

    private void findNodeByClassName(AccessibilityNodeInfo node, String className, List<AccessibilityNodeInfo> result) {
        if (node == null) return;
        if (node.getClassName() != null && node.getClassName().toString().equals(className)) result.add(node);
        for (int i = 0; i < node.getChildCount(); i++) findNodeByClassName(node.getChild(i), className, result);
    }
    @Override
    public void onInterrupt() {}
}
