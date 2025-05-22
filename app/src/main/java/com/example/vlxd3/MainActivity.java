// File: MainActivity.java

package com.example.vlxd3;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast; // Thêm import này nếu chưa có

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.vlxd3.dao.UserDAO;
import com.example.vlxd3.model.User;

public class MainActivity extends AppCompatActivity {

    private int userId; // Biến để lưu userId
    private User user;
    private UserDAO userDAO;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // NHẬN userId TỪ INTENT
        userId = getIntent().getIntExtra("userId", -1); // <-- THÊM DÒNG NÀY

        // Kiểm tra userId, nếu là -1 (chưa đăng nhập), chuyển về LoginActivity
        if (userId == -1) {
            Toast.makeText(this, "Vui lòng đăng nhập để sử dụng chức năng này!", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(this, LoginActivity.class);
            startActivity(intent);
            finish(); // Đóng MainActivity để tránh người dùng quay lại khi chưa đăng nhập
            return; // Kết thúc onCreate
        }

        userDAO = new UserDAO(this);
        user = userDAO.getUserById(userId);
        TextView textViewTitle = findViewById(R.id.textViewTitle);
        if (textViewTitle != null && user != null) {
            textViewTitle.setText("Xin chào, " + user.getFullName());
        }

        // Thêm sự kiện click cho textViewSeeAllCriteria
        TextView textViewSeeAllCriteria = findViewById(R.id.textViewSeeAllCriteria);
        if (textViewSeeAllCriteria != null) {
            textViewSeeAllCriteria.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(MainActivity.this, ActivityCategory.class);
                    intent.putExtra("userId", userId); // <-- TRUYỀN userId
                    startActivity(intent);
                }
            });
        }

        // Xử lý bottom navigation
        LinearLayout bottomNav = findViewById(R.id.bottomNavigationView);
        if (bottomNav != null && bottomNav.getChildCount() >= 2) {
            LinearLayout homeLayout = (LinearLayout) bottomNav.getChildAt(0);
            LinearLayout basketLayout = (LinearLayout) bottomNav.getChildAt(1);
            LinearLayout accountLayout = (LinearLayout) bottomNav.getChildAt(2); // Giả sử có 3 item

            homeLayout.setOnClickListener(v -> {
                // Đã ở MainActivity, có thể refresh hoặc không làm gì
            });
            basketLayout.setOnClickListener(v -> {
                Intent intent = new Intent(MainActivity.this, ActivityBasket.class);
                intent.putExtra("userId", userId); // <-- TRUYỀN userId
                startActivity(intent);
            });
            accountLayout.setOnClickListener(v -> {
                // Chuyển đến Activity tài khoản (nếu có)
                // Intent intent = new Intent(MainActivity.this, ActivityAccount.class);
                // intent.putExtra("userId", userId);
                // startActivity(intent);
                Toast.makeText(MainActivity.this, "Chức năng tài khoản đang được phát triển!", Toast.LENGTH_SHORT).show();
            });
        }
    }
}