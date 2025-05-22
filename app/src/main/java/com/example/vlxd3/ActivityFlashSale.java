// File: ActivityFlashSale.java

package com.example.vlxd3;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageView; // Thêm import này
import android.widget.ListView;
import android.widget.Toast; // Thêm import này

import androidx.appcompat.app.AppCompatActivity;

import com.example.vlxd3.dao.FlashSaleDAO;
import com.example.vlxd3.dao.ProductDAO; // Thêm import này
import com.example.vlxd3.model.FlashSale;
import com.example.vlxd3.model.Product;

import java.util.List;

public class ActivityFlashSale extends AppCompatActivity {
    private ListView flashSaleListView;
    private FlashSaleAdapter adapter;
    private FlashSaleDAO flashSaleDAO;
    private ProductDAO productDAO; // Khai báo ProductDAO để có thể lấy Product từ FlashSale
    private int userId; // Biến để lưu userId

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_flash_sale);

        // Thêm nút back
        ImageView backButton = findViewById(R.id.backButton);
        if (backButton != null) {
            backButton.setOnClickListener(v -> finish());
        }

        // Nhận userId từ Intent
        userId = getIntent().getIntExtra("userId", -1);
        if (userId == -1) {
            Toast.makeText(this, "Bạn cần đăng nhập để xem flash sales!", Toast.LENGTH_SHORT).show();
            Intent loginIntent = new Intent(this, LoginActivity.class);
            startActivity(loginIntent);
            finish();
            return;
        }

        flashSaleListView = findViewById(R.id.FlashSaleListView);
        flashSaleDAO = new FlashSaleDAO(this);
        productDAO = new ProductDAO(this); // Khởi tạo ProductDAO

        // Lấy danh sách Flash Sale (List<FlashSale>)
        List<FlashSale> flashSales = flashSaleDAO.getAllFlashSales();

        // Gán adapter cho ListView, truyền List<FlashSale>
        adapter = new FlashSaleAdapter(this, flashSales); // <-- Đã sửa ở đây
        flashSaleListView.setAdapter(adapter);

        // Xử lý sự kiện click vào sản phẩm
        flashSaleListView.setOnItemClickListener((AdapterView<?> parent, View view, int position, long id) -> {
            FlashSale selectedFlashSale = flashSales.get(position);
            // Truyền productId và userId đến ActivityProductDetail
            Intent intent = new Intent(ActivityFlashSale.this, ActivityProductDetail.class);
            intent.putExtra("productId", selectedFlashSale.getProductId());
            intent.putExtra("userId", userId); // Truyền userId
            // Không cần "isFlashSale" ở đây nếu ActivityProductDetail tự động kiểm tra FlashSaleDAO
            startActivity(intent);
        });
    }
}