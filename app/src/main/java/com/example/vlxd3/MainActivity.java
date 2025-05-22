// File: MainActivity.java

package com.example.vlxd3;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ImageView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.vlxd3.dao.CategoryDAO;
import com.example.vlxd3.dao.FlashSaleDAO; // Import FlashSaleDAO
import com.example.vlxd3.dao.UserDAO;
import com.example.vlxd3.model.Category;
import com.example.vlxd3.model.FlashSale; // Import FlashSale
import com.example.vlxd3.model.User;

import java.util.List;

public class MainActivity extends AppCompatActivity {

    private int userId;
    private User user;
    private UserDAO userDAO;
    private RecyclerView recyclerViewCategories;
    private CategoryDAO categoryDAO;
    private RecyclerView recyclerViewFlashSales; // Khai báo RecyclerView cho Flash Sale
    private FlashSaleDAO flashSaleDAO; // Khai báo FlashSaleDAO

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
        TextView textViewTitle = findViewById(R.id.textViewTitle);
        if (textViewTitle != null && user != null) {
            textViewTitle.setText("Xin chào, " + user.getFullName());
        }

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
        recyclerViewCategories = findViewById(R.id.recyclerViewCriteria);
        categoryDAO = new CategoryDAO(this);

        List<Category> allCategories = categoryDAO.getAllCategories();
        List<Category> displayedCategories = allCategories; // Lấy tất cả hoặc giới hạn số lượng

        recyclerViewCategories.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        CategoryMainAdapter categoryMainAdapter = new CategoryMainAdapter(this, displayedCategories, userId);
        recyclerViewCategories.setAdapter(categoryMainAdapter);


        // Xử lý RecyclerView cho Sản phẩm Flash Sale
        recyclerViewFlashSales = findViewById(R.id.recyclerViewProducts); // Ánh xạ RecyclerView cho Flash Sale
        flashSaleDAO = new FlashSaleDAO(this); // Khởi tạo FlashSaleDAO

        // Lấy danh sách các sản phẩm flash sale (ví dụ: tất cả hoặc chỉ một vài sản phẩm đầu tiên)
        List<FlashSale> allFlashSales = flashSaleDAO.getAllFlashSales();
        // Giả sử bạn muốn hiển thị một số sản phẩm flash sale nổi bật
        // List<FlashSale> displayedFlashSales = allFlashSales.subList(0, Math.min(allFlashSales.size(), 5));
        // Để đơn giản, hiển thị tất cả các flash sale
        List<FlashSale> displayedFlashSales = allFlashSales;


        recyclerViewFlashSales.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        FlashSaleMainAdapter flashSaleMainAdapter = new FlashSaleMainAdapter(this, displayedFlashSales, userId);
        recyclerViewFlashSales.setAdapter(flashSaleMainAdapter);


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
            // Thêm sự kiện cho "Xem tất cả" Flash Sale nếu bạn có nút đó trong layout
            TextView textViewSeeAllProfitable = findViewById(R.id.textViewSeeAllProfitable);
            if (textViewSeeAllProfitable != null) {
                textViewSeeAllProfitable.setOnClickListener(v -> {
                    Intent intent = new Intent(MainActivity.this, ActivityFlashSale.class);
                    intent.putExtra("userId", userId);
                    startActivity(intent);
                });
            }
        }
    }
}