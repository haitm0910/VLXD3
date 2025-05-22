// File: MainActivity.java
package com.example.vlxd3;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ImageView; // Import ImageView

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager; // Import LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView; // Import RecyclerView

import com.example.vlxd3.dao.CategoryDAO; // Import CategoryDAO
import com.example.vlxd3.dao.UserDAO;
import com.example.vlxd3.model.Category; // Import Category
import com.example.vlxd3.model.User;

import java.util.List;

public class MainActivity extends AppCompatActivity {

    private int userId;
    private User user;
    private UserDAO userDAO;
    private RecyclerView recyclerViewCategories; // Khai báo RecyclerView
    private CategoryDAO categoryDAO; // Khai báo CategoryDAO

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

        userId = getIntent().getIntExtra("userId", -1);

        if (userId == -1) {
            Toast.makeText(this, "Vui lòng đăng nhập để sử dụng chức năng này!", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(this, LoginActivity.class);
            startActivity(intent);
            finish();
            return;
        }

        userDAO = new UserDAO(this);
        user = userDAO.getUserById(userId);
        TextView textViewTitle = findViewById(R.id.textViewTitle); // TextView để hiển thị tên người dùng
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
                    intent.putExtra("userId", userId);
                    startActivity(intent);
                }
            });
        }

        // Xử lý RecyclerView cho Danh mục sản phẩm
        recyclerViewCategories = findViewById(R.id.recyclerViewCriteria); // Ánh xạ RecyclerView
        categoryDAO = new CategoryDAO(this); // Khởi tạo CategoryDAO

        // Lấy danh sách danh mục (có thể giới hạn số lượng nếu chỉ muốn hiển thị 3-4 danh mục nổi bật)
        List<Category> allCategories = categoryDAO.getAllCategories();
        // Giả sử bạn muốn hiển thị 3-4 danh mục đầu tiên
        // List<Category> displayedCategories = allCategories.subList(0, Math.min(allCategories.size(), 4));
        // Để đơn giản, hiển thị tất cả các danh mục
        List<Category> displayedCategories = allCategories;

        // Thiết lập LayoutManager (cuộn ngang)
        recyclerViewCategories.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        // Tạo và gán Adapter
        CategoryMainAdapter categoryMainAdapter = new CategoryMainAdapter(this, displayedCategories, userId);
        recyclerViewCategories.setAdapter(categoryMainAdapter);


        // Xử lý bottom navigation
        LinearLayout bottomNav = findViewById(R.id.bottomNavigationView);
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
                // Đã ở MainActivity, có thể refresh hoặc không làm gì
            });
            basketLayout.setOnClickListener(v -> {
                Intent intent = new Intent(MainActivity.this, ActivityBasket.class);
                intent.putExtra("userId", userId);
                startActivity(intent);
            });
            accountLayout.setOnClickListener(v -> {
                Intent intent = new Intent(MainActivity.this, AccountActivity.class);
                intent.putExtra("userId", userId);
                startActivity(intent);
            });
        }
    }
}