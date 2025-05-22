// File: AccountActivity.java

package com.example.vlxd3;

import android.content.Intent;
import android.os.Bundle;
import android.view.View; // Thêm import này
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Button; // Thêm import này nếu có nút Đăng xuất
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.vlxd3.dao.UserDAO;
import com.example.vlxd3.model.User;

public class AccountActivity extends AppCompatActivity {

    private int userId;
    private UserDAO userDAO;
    private TextView accountNameHeaderTextView; // TextView mới cho tên tài khoản ở header
    private LinearLayout optionLogout; // LinearLayout cho tùy chọn Đăng xuất

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_account); // Đảm bảo sử dụng đúng layout activity_account.xml

        // Ánh xạ TextView cho tên tài khoản ở header
        accountNameHeaderTextView = findViewById(R.id.tv_account_name_header);

        // Ánh xạ LinearLayout cho tùy chọn Đăng xuất
        optionLogout = findViewById(R.id.option_dang_xuat);

        // Nhận userId từ Intent
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
            // Hiển thị tên người dùng trên header
            accountNameHeaderTextView.setText(currentUser.getFullName());

            // TODO: Bạn có thể thêm các TextView khác trong activity_account.xml
            //       để hiển thị chi tiết hơn như username, phone, email, v.v.
            //       Hiện tại, layout chỉ có tv_account_name_header cho tên.

            // Xử lý nút Đăng xuất
            if (optionLogout != null) {
                optionLogout.setOnClickListener(v -> {
                    // Logic đăng xuất: Chuyển về màn hình đăng nhập và xóa userId
                    Toast.makeText(AccountActivity.this, "Đã đăng xuất!", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(AccountActivity.this, LoginActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK); // Xóa tất cả Activity trên stack
                    startActivity(intent);
                    finish(); // Đóng AccountActivity
                });
            }

        } else {
            Toast.makeText(this, "Không thể tải thông tin tài khoản.", Toast.LENGTH_SHORT).show();
            accountNameHeaderTextView.setText("Người dùng"); // Đặt tên mặc định nếu không tải được
        }

//        // Xử lý nút back (nếu bạn muốn có nút back ở đây)
//        ImageView backButton = findViewById(R.id.backButtonAccount);
//        if (backButton != null) {
//            backButton.setOnClickListener(v -> finish());
//        }

        // Xử lý bottom navigation (tương tự như MainActivity và ActivityBasket)
        LinearLayout bottomNav = findViewById(R.id.bottom_navigation_bar); // Đảm bảo ID này đúng
        if (bottomNav != null && bottomNav.getChildCount() >= 2) {
            LinearLayout homeLayout = (LinearLayout) bottomNav.getChildAt(0);
            LinearLayout basketLayout = (LinearLayout) bottomNav.getChildAt(1);
            LinearLayout accountLayout = (LinearLayout) bottomNav.getChildAt(2);

            // Đặt màu cho các icon bottom nav (đảm bảo các ImageView có ID)
            ImageView homeIcon = homeLayout.findViewById(R.id.home_icon_bottom_nav);
            if(homeIcon != null) homeIcon.setImageResource(R.drawable.home); // Đặt lại src vì tint đã được đặt
            ImageView basketIcon = basketLayout.findViewById(R.id.basket_icon_bottom_nav);
            if(basketIcon != null) basketIcon.setImageResource(R.drawable.basket);
            ImageView accountIcon = accountLayout.findViewById(R.id.account_icon_bottom_nav);
            if(accountIcon != null) accountIcon.setImageResource(R.drawable.account); // Đây là tab hiện tại, nên đặt màu active

            homeLayout.setOnClickListener(v -> {
                Intent intent = new Intent(AccountActivity.this, MainActivity.class);
                intent.putExtra("userId", userId);
                startActivity(intent);
                finish(); // Đóng Activity hiện tại
            });
            basketLayout.setOnClickListener(v -> {
                Intent intent = new Intent(AccountActivity.this, ActivityBasket.class);
                intent.putExtra("userId", userId);
                startActivity(intent);
                finish(); // Đóng Activity hiện tại
            });
            accountLayout.setOnClickListener(v -> {
                Toast.makeText(AccountActivity.this, "Bạn đang ở màn hình Tài khoản!", Toast.LENGTH_SHORT).show();
            });
        }
    }
}