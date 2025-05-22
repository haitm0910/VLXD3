// File: MainActivity.java

package com.example.vlxd3;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText; // Import EditText
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
import com.example.vlxd3.dao.ProductDAO; // Import ProductDAO
import com.example.vlxd3.dao.UserDAO;
import com.example.vlxd3.model.Category;
import com.example.vlxd3.model.FlashSale;
import com.example.vlxd3.model.Product; // Import Product
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

    private EditText searchEditText; // Khai báo EditText tìm kiếm
    private ImageView searchIcon; // Khai báo ImageView nút tìm kiếm
    private ProductDAO productDAO; // Khai báo ProductDAO để tìm kiếm


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

        // Ánh xạ EditText và ImageView tìm kiếm
        searchEditText = findViewById(R.id.editTextSearch);
        searchIcon = findViewById(R.id.searchIcon);
        productDAO = new ProductDAO(this); // Khởi tạo ProductDAO

        // Xử lý sự kiện click cho nút tìm kiếm
        if (searchIcon != null) {
            searchIcon.setOnClickListener(v -> performSearch());
        }
        // Có thể thêm OnEditorActionListener cho searchEditText để tìm kiếm khi nhấn Enter trên bàn phím
        // searchEditText.setOnEditorActionListener((v, actionId, event) -> {
        //     if (actionId == EditorInfo.IME_ACTION_SEARCH || actionId == EditorInfo.IME_ACTION_DONE || event.getAction() == KeyEvent.ACTION_DOWN && event.getKeyCode() == KeyEvent.KEYCODE_ENTER) {
        //         performSearch();
        //         return true;
        //     }
        //     return false;
        // });


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
        List<Category> displayedCategories = allCategories;

        recyclerViewCategories.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        CategoryMainAdapter categoryMainAdapter = new CategoryMainAdapter(this, displayedCategories, userId);
        recyclerViewCategories.setAdapter(categoryMainAdapter);

        // Xử lý RecyclerView cho Sản phẩm Flash Sale
        recyclerViewFlashSales = findViewById(R.id.recyclerViewProducts);
        flashSaleDAO = new FlashSaleDAO(this);

        List<FlashSale> allFlashSales = flashSaleDAO.getAllFlashSales();
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

    // PHƯƠNG THỨC XỬ LÝ TÌM KIẾM
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
            // TODO: Hiển thị kết quả tìm kiếm
            // Cách 1: Hiển thị trong một Activity mới (ví dụ ActivitySearchResults)
            Intent intent = new Intent(MainActivity.this, ActivityGachOpLat.class); // Tái sử dụng ActivityGachOpLat cho kết quả tìm kiếm
            // Bạn cần điều chỉnh ActivityGachOpLat để nó có thể nhận List<Product> hoặc một query string
            // và hiển thị các sản phẩm đó.
            // Để đơn giản, tôi sẽ chuyển sang ActivityGachOpLat và gửi một query string
            // Bạn sẽ cần sửa ActivityGachOpLat để nó đọc query string và gọi productDAO.searchProductsByName()

            intent.putExtra("searchQuery", query); // Truyền chuỗi tìm kiếm
            intent.putExtra("userId", userId); // Đảm bảo userId cũng được truyền
            startActivity(intent);

            // Cách 2: Hiển thị ngay trên MainActivity (phức tạp hơn, cần thay đổi RecyclerView hoặc thêm một ListView/RecyclerView mới)
            // Hiện tại, chúng ta sẽ chuyển sang Activity mới để giữ MainActivity gọn gàng.
        }
    }
}