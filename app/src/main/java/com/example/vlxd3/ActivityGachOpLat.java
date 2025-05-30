// File: ActivityGachOpLat.java

package com.example.vlxd3;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView; // Import TextView
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.vlxd3.dao.ProductDAO;
import com.example.vlxd3.model.Product;

import java.util.ArrayList;
import java.util.List;

public class ActivityGachOpLat extends AppCompatActivity {
    private ListView listViewProducts;
    private ProductDAO productDAO;
    private List<Product> productList;
    private ProductAdapter adapter;
    private int userId;
    private TextView textViewProductTitle; // Ánh xạ TextView tiêu đề

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gach_op_lat);

        listViewProducts = findViewById(R.id.listViewProducts);
        textViewProductTitle = findViewById(R.id.textViewProductTitle); // Ánh xạ TextView tiêu đề
        ImageView backButton = findViewById(R.id.backButtonGachOpLat); // Ánh xạ ImageView của nút back
        if (backButton != null) { // Kiểm tra để tránh NullPointerException nếu nút không tìm thấy
            backButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    finish(); // Phương thức này sẽ đóng Activity hiện tại và quay về Activity trước đó
                }
            });
        }
        productDAO = new ProductDAO(this);
        userId = getIntent().getIntExtra("userId", -1);

        // KIỂM TRA XEM CÓ CHUỖI TÌM KIẾM KHÔNG
        String searchQuery = getIntent().getStringExtra("searchQuery");
        if (searchQuery != null && !searchQuery.isEmpty()) {
            // Đây là màn hình kết quả tìm kiếm
            textViewProductTitle.setText("Kết quả tìm kiếm cho: \"" + searchQuery + "\"");
            productList = productDAO.searchProductsByName(searchQuery); // Gọi phương thức tìm kiếm
        } else {
            // Đây là màn hình danh mục sản phẩm thông thường
            int categoryId = getIntent().getIntExtra("categoryId", -1);
            if (categoryId != -1) {
                // TODO: Lấy tên danh mục để hiển thị trên tiêu đề
                // CategoryDAO categoryDAO = new CategoryDAO(this);
                // Category category = categoryDAO.getCategoryById(categoryId);
                // if (category != null) {
                //     textViewProductTitle.setText(category.getName());
                // } else {
                //     textViewProductTitle.setText("Sản phẩm theo danh mục");
                // }
                textViewProductTitle.setText("Sản phẩm theo danh mục"); // Hoặc lấy tên danh mục cụ thể
                productList = productDAO.getProductsByCategory(categoryId);
            } else {
                // Trường hợp lỗi: không có categoryId và không có searchQuery
                textViewProductTitle.setText("Danh sách sản phẩm");
                productList = new ArrayList<>(); // Danh sách rỗng
                Toast.makeText(this, "Không có sản phẩm để hiển thị.", Toast.LENGTH_SHORT).show();
            }
        }

        adapter = new ProductAdapter(this, productList, userId);
        listViewProducts.setAdapter(adapter);

    }
}