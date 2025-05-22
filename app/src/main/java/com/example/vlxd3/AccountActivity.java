// File: AccountActivity.java

package com.example.vlxd3;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.vlxd3.dao.UserDAO;
import com.example.vlxd3.model.User;

public class AccountActivity extends AppCompatActivity {

    private int userId;
    private UserDAO userDAO;
    private TextView accountNameHeaderTextView;
    private LinearLayout optionLogout;
    private LinearLayout optionPersonalInfo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_account);

        accountNameHeaderTextView = findViewById(R.id.tv_account_name_header);
        optionLogout = findViewById(R.id.option_dang_xuat);
        optionPersonalInfo = findViewById(R.id.option_thong_tin_ca_nhan);

        userId = getIntent().getIntExtra("userId", -1);

        if (userId == -1) {
            Toast.makeText(this, "Không tìm thấy thông tin người dùng. Vui lòng đăng nhập lại.", Toast.LENGTH_LONG).show();
            Intent loginIntent = new Intent(this, LoginActivity.class);
            startActivity(loginIntent);
            finish();
            return;
        }

        userDAO = new UserDAO(this);
        User currentUser = userDAO.getUserById(userId);

        if (currentUser != null) {
            accountNameHeaderTextView.setText(currentUser.getFullName());

            if (optionLogout != null) {
                optionLogout.setOnClickListener(v -> {
                    Toast.makeText(AccountActivity.this, "Đã đăng xuất!", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(AccountActivity.this, LoginActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    finish();
                });
            }

            // SỬA SỰ KIỆN CLICK CHO TÙY CHỌN "THÔNG TIN CÁ NHÂN"
            if (optionPersonalInfo != null) {
                optionPersonalInfo.setOnClickListener(v -> {
                    Intent intent = new Intent(AccountActivity.this, PersonalActivity.class); // <-- Đã sửa ở đây
                    intent.putExtra("userId", userId);
                    startActivity(intent);
                });
            }

        } else {
            Toast.makeText(this, "Không thể tải thông tin tài khoản.", Toast.LENGTH_SHORT).show();
            accountNameHeaderTextView.setText("Người dùng");
        }

        ImageView backButton = findViewById(R.id.iv_back_arrow_account);
        if (backButton != null) {
            backButton.setOnClickListener(v -> finish());
        }

        LinearLayout bottomNav = findViewById(R.id.bottom_navigation_bar);
        if (bottomNav != null && bottomNav.getChildCount() >= 2) {
            LinearLayout homeLayout = (LinearLayout) bottomNav.getChildAt(0);
            LinearLayout basketLayout = (LinearLayout) bottomNav.getChildAt(1);
            LinearLayout accountLayout = (LinearLayout) bottomNav.getChildAt(2);

            ImageView homeIcon = homeLayout.findViewById(R.id.home_icon_bottom_nav);
            if(homeIcon != null) homeIcon.setImageResource(R.drawable.home);
            ImageView basketIcon = basketLayout.findViewById(R.id.basket_icon_bottom_nav);
            if(basketIcon != null) basketIcon.setImageResource(R.drawable.basket);
            ImageView accountIcon = accountLayout.findViewById(R.id.account_icon_bottom_nav);
            if(accountIcon != null) accountIcon.setImageResource(R.drawable.account);

            homeLayout.setOnClickListener(v -> {
                Intent intent = new Intent(AccountActivity.this, MainActivity.class);
                intent.putExtra("userId", userId);
                startActivity(intent);
                finish();
            });
            basketLayout.setOnClickListener(v -> {
                Intent intent = new Intent(AccountActivity.this, ActivityBasket.class);
                intent.putExtra("userId", userId);
                startActivity(intent);
                finish();
            });
            accountLayout.setOnClickListener(v -> {
                Toast.makeText(AccountActivity.this, "Bạn đang ở màn hình Tài khoản!", Toast.LENGTH_SHORT).show();
            });
        }
    }
}