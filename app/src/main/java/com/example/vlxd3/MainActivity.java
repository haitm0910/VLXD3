// File: MainActivity.java (Không có thay đổi nào lớn ở đây)

package com.example.vlxd3;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
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
import com.example.vlxd3.dao.FlashSaleDAO;
import com.example.vlxd3.dao.ProductDAO;
import com.example.vlxd3.dao.UserDAO;
import com.example.vlxd3.model.Category;
import com.example.vlxd3.model.FlashSale;
import com.example.vlxd3.model.Product;
import com.example.vlxd3.model.User;

import java.util.List;

public class MainActivity extends AppCompatActivity {

    private int userId;
    private User user;
    private UserDAO userDAO;
    private RecyclerView recyclerViewCategories;
    private CategoryDAO categoryDAO;
    private RecyclerView recyclerViewFlashSales;
    private FlashSaleDAO flashSaleDAO;

    private EditText searchEditText;
    private ImageView searchIcon;
    private ProductDAO productDAO;


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

        searchEditText = findViewById(R.id.editTextSearch);
        searchIcon = findViewById(R.id.searchIcon);
        productDAO = new ProductDAO(this);

        if (searchIcon != null) {
            searchIcon.setOnClickListener(v -> performSearch());
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

        recyclerViewCategories = findViewById(R.id.recyclerViewCriteria);
        categoryDAO = new CategoryDAO(this);

        List<Category> allCategories = categoryDAO.getAllCategories();
        List<Category> displayedCategories = allCategories;

        recyclerViewCategories.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        CategoryMainAdapter categoryMainAdapter = new CategoryMainAdapter(this, displayedCategories, userId);
        recyclerViewCategories.setAdapter(categoryMainAdapter);

        recyclerViewFlashSales = findViewById(R.id.recyclerViewProducts);
        flashSaleDAO = new FlashSaleDAO(this);

        List<FlashSale> allFlashSales = flashSaleDAO.getAllFlashSales();
        List<FlashSale> displayedFlashSales = allFlashSales;

        recyclerViewFlashSales.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        FlashSaleMainAdapter flashSaleMainAdapter = new FlashSaleMainAdapter(this, displayedFlashSales, userId);
        recyclerViewFlashSales.setAdapter(flashSaleMainAdapter);


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

    private void performSearch() {
        String query = searchEditText.getText().toString().trim();
        if (query.isEmpty()) {
            Toast.makeText(this, "Vui lòng nhập từ khóa tìm kiếm!", Toast.LENGTH_SHORT).show();
            return;
        }

        List<Product> searchResults = productDAO.searchProductsByName(query);

        if (searchResults.isEmpty()) {
            Toast.makeText(this, "Không tìm thấy sản phẩm nào phù hợp!", Toast.LENGTH_LONG).show();
        } else {
            Intent intent = new Intent(MainActivity.this, ActivityGachOpLat.class);
            intent.putExtra("searchQuery", query);
            intent.putExtra("userId", userId);
            startActivity(intent);
        }
    }
}