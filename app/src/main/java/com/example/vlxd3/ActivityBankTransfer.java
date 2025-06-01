package com.example.vlxd3; // Giữ nguyên package của bạn

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;

import java.nio.charset.StandardCharsets;
import java.util.Locale;

public class ActivityBankTransfer extends AppCompatActivity {
    private static final String BANK_NAME_DISPLAY = "Ngân hàng TMCP Ngoại thương Việt Nam (Vietcombank)";
    private static final String ACCOUNT_NUMBER = "1026349848";
    private static final String ACCOUNT_NAME_DISPLAY = "NGUYEN MINH PHUONG";
    private static final String BANK_BIN = "970436"; // Vietcombank BIN

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bank_transfer);

        TextView bankNameText = findViewById(R.id.bank_name_text);
        TextView accountNumberText = findViewById(R.id.account_number_text);
        TextView accountNameText = findViewById(R.id.account_name_text);
        TextView amountText = findViewById(R.id.amount_text);
        TextView contentText = findViewById(R.id.content_text);
        ImageView qrImage = findViewById(R.id.qr_image);

        Button copyBankName = findViewById(R.id.copy_bank_name_btn);
        Button copyAccountNumber = findViewById(R.id.copy_account_number_btn);
        Button copyAccountName = findViewById(R.id.copy_account_name_btn);
        Button copyAmount = findViewById(R.id.copy_amount_btn);
        Button copyContent = findViewById(R.id.copy_content_btn);

        Intent intent = getIntent();
        double amount = intent.getDoubleExtra("amount", 0);
        long orderId = intent.getLongExtra("orderId", 0);
        String contentTransfer = "VLXD" + orderId;

        bankNameText.setText(BANK_NAME_DISPLAY);
        accountNumberText.setText(ACCOUNT_NUMBER);
        accountNameText.setText(ACCOUNT_NAME_DISPLAY);
        amountText.setText(String.format(Locale.ROOT, "%,.0f đ", amount));
        contentText.setText(contentTransfer);

        copyBankName.setOnClickListener(v -> copyToClipboard("Tên ngân hàng", BANK_NAME_DISPLAY));
        copyAccountNumber.setOnClickListener(v -> copyToClipboard("Số tài khoản", ACCOUNT_NUMBER));
        copyAccountName.setOnClickListener(v -> copyToClipboard("Tên chủ tài khoản", ACCOUNT_NAME_DISPLAY));
        copyAmount.setOnClickListener(v -> copyToClipboard("Số tiền", String.format(Locale.ROOT, "%.0f", amount)));
        copyContent.setOnClickListener(v -> copyToClipboard("Nội dung", contentTransfer));

        String qrData = generateVietQRData(BANK_BIN, ACCOUNT_NUMBER, amount, contentTransfer);

        if (qrData != null) {
            Log.d("VietQRDebug", "Generated QR Data (NEW Tag38 Structure): " + qrData);
        } else {
            Log.e("VietQRDebug", "Generated QR Data is NULL");
        }

        Bitmap qrBitmap = generateQRCodeImage(qrData, 600, 600);
        if (qrBitmap != null) {
            qrImage.setImageBitmap(qrBitmap);
        } else {
            Toast.makeText(this, "Lỗi tạo mã QR", Toast.LENGTH_SHORT).show();
        }
    }

    private void copyToClipboard(String label, String text) {
        ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
        ClipData clip = ClipData.newPlainText(label, text);
        clipboard.setPrimaryClip(clip);
        Toast.makeText(this, "Đã sao chép: " + label, Toast.LENGTH_SHORT).show();
    }

    private Bitmap generateQRCodeImage(String data, int width, int height) {
        if (data == null || data.isEmpty()) {
            Log.e("QRCodeGenerator", "Data for QR code is null or empty");
            return null;
        }
        QRCodeWriter writer = new QRCodeWriter();
        try {
            BitMatrix bitMatrix = writer.encode(data, BarcodeFormat.QR_CODE, width, height);
            Bitmap bmp = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565);
            for (int x = 0; x < width; x++) {
                for (int y = 0; y < height; y++) {
                    bmp.setPixel(x, y, bitMatrix.get(x, y) ? 0xFF000000 : 0xFFFFFFFF);
                }
            }
            return bmp;
        } catch (WriterException e) {
            Log.e("QRCodeGenerator", "Error generating QR code image", e);
        }
        return null;
    }

    private String generateVietQRData(String bankBin, String accountNumber, double amount, String content) {
        String vietQrAid = "A000000727";
        String serviceCode = "QRIBFTTA"; // NAPAS 247 Account-based Transfer

        StringBuilder sb = new StringBuilder();
        boolean isDynamicQR = amount > 0;

        // Tag 00: Payload Format Indicator
        sb.append("000201");

        // Tag 01: Point of Initiation Method
        if (isDynamicQR) {
            sb.append("010212"); // Dynamic QR
        } else {
            sb.append("010211"); // Static QR
        }

        // Tag 38: Merchant Account Information (Theo cấu trúc của Vietcombank / NAPAS)
        StringBuilder tag38ValueBuilder = new StringBuilder();
        //  Sub-tag 00: GUID (VietQR AID)
        tag38ValueBuilder.append("00").append(String.format(Locale.ROOT, "%02d", vietQrAid.length())).append(vietQrAid);

        //  Sub-tag 01: Beneficiary Information (chứa Bank BIN và Account Number lồng nhau)
        StringBuilder beneficiaryInfoBuilder = new StringBuilder();
        //    Sub-sub-tag 00: Bank BIN
        beneficiaryInfoBuilder.append("00").append(String.format(Locale.ROOT, "%02d", bankBin.length())).append(bankBin);
        //    Sub-sub-tag 01: Account Number
        beneficiaryInfoBuilder.append("01").append(String.format(Locale.ROOT, "%02d", accountNumber.length())).append(accountNumber);

        String beneficiaryInfoValue = beneficiaryInfoBuilder.toString();
        tag38ValueBuilder.append("01").append(String.format(Locale.ROOT, "%02d", beneficiaryInfoValue.length())).append(beneficiaryInfoValue);

        //  Sub-tag 02: Service Code (Theo cấu trúc của Vietcombank / NAPAS)
        tag38ValueBuilder.append("02").append(String.format(Locale.ROOT, "%02d", serviceCode.length())).append(serviceCode);

        String tag38Value = tag38ValueBuilder.toString();
        sb.append("38").append(String.format(Locale.ROOT, "%02d", tag38Value.length())).append(tag38Value);

        // Tag 53: Transaction Currency (VND)
        sb.append("5303704");

        // Tag 54: Transaction Amount - Chỉ thêm nếu là QR động
        if (isDynamicQR) {
            String amountStr = String.format(Locale.ROOT, "%.0f", amount);
            sb.append("54").append(String.format(Locale.ROOT, "%02d", amountStr.length())).append(amountStr);
        }

        // Tag 58: Country Code (VN)
        sb.append("5802VN");

        // Tag 62: Additional Data Field Template
        if (content != null && !content.isEmpty() && isDynamicQR) { // Thường chỉ có ý nghĩa với QR động
            StringBuilder sb62 = new StringBuilder();
            String sanitizedContent = removeVietnameseTones(content).toUpperCase();
            if (sanitizedContent.length() > 25) {
                sanitizedContent = sanitizedContent.substring(0, 25);
            }

            if (!sanitizedContent.isEmpty()){
                // SubTag 08: Purpose of Transaction
                sb62.append("08").append(String.format(Locale.ROOT, "%02d", sanitizedContent.length())).append(sanitizedContent);
            }

            String data62 = sb62.toString();
            if (!data62.isEmpty()) {
                sb.append("62").append(String.format(Locale.ROOT, "%02d", data62.length())).append(data62);
            }
        }

        // Tag 63: CRC
        sb.append("6304");
        String stringToCRC = sb.toString();
        String crc = calculateCRC16_CCITT_FALSE(stringToCRC.getBytes(StandardCharsets.UTF_8));
        sb.append(crc);

        return sb.toString();
    }

    private String removeVietnameseTones(String str) {
        if (str == null) return "";
        str = str.replaceAll("[àáạảãâầấậẩẫăằắặẳẵ]", "a");
        str = str.replaceAll("[èéẹẻẽêềếệểễ]", "e");
        str = str.replaceAll("[ìíịỉĩ]", "i");
        str = str.replaceAll("[òóọỏõôồốộổỗơờớợởỡ]", "o");
        str = str.replaceAll("[ùúụủũưừứựửữ]", "u");
        str = str.replaceAll("[ỳýỵỷỹ]", "y");
        str = str.replaceAll("đ", "d");
        str = str.replaceAll("[ÀÁẠẢÃÂẦẤẬẨẪĂẰẮẶẲẴ]", "A");
        str = str.replaceAll("[ÈÉẸẺẼÊỀẾỆỂỄ]", "E");
        str = str.replaceAll("[ÌÍỊỈĨ]", "I");
        str = str.replaceAll("[ÒÓỌỎÕÔỒỐỘỔỖƠỜỚỢỞỠ]", "O");
        str = str.replaceAll("[ÙÚỤỦŨƯỪỨỰỬỮ]", "U");
        str = str.replaceAll("[ỲÝỴỶỸ]", "Y");
        str = str.replaceAll("Đ", "D");
        return str;
    }

    private String calculateCRC16_CCITT_FALSE(byte[] data) {
        int crc = 0xFFFF;
        int polynomial = 0x1021; // CCITT polynomial

        for (byte b : data) {
            crc ^= (b & 0xFF) << 8;
            for (int i = 0; i < 8; i++) {
                if ((crc & 0x8000) != 0) {
                    crc = (crc << 1) ^ polynomial;
                } else {
                    crc <<= 1;
                }
            }
        }
        crc &= 0xFFFF; // Ensure CRC is 16 bit
        return String.format(Locale.ROOT, "%04X", crc).toUpperCase();
    }
}