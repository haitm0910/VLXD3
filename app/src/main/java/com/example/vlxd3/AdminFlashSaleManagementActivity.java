// File: AdminFlashSaleManagementActivity.java

package com.example.vlxd3;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.vlxd3.dao.FlashSaleDAO;
import com.example.vlxd3.model.FlashSale;

import java.util.List;

public class AdminFlashSaleManagementActivity extends AppCompatActivity implements AdminFlashSaleAdapter.OnFlashSaleActionListener {

    private RecyclerView adminFlashSaleRecyclerView;
    private TextView emptyFlashSaleMessage;
    private ImageView backButton;
    private ImageView addFlashSaleButton; // Nút thêm Flash Sale

    private FlashSaleDAO flashSaleDAO;
    private AdminFlashSaleAdapter adapter;
    private int adminUserId; // userId của admin

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_flash_sale_management);

        // Ánh xạ Views
        adminFlashSaleRecyclerView = findViewById(R.id.rv_admin_flash_sale_list);
        emptyFlashSaleMessage = findViewById(R.id.empty_flash_sale_message);
        backButton = findViewById(R.id.backButtonAdminFlashSale);
        addFlashSaleButton = findViewById(R.id.addFlashSaleButton);

        adminUserId = getIntent().getIntExtra("userId", -1); // Lấy userId của admin

        // Khởi tạo DAO
        flashSaleDAO = new FlashSaleDAO(this);

        // Xử lý nút back
        if (backButton != null) {
            backButton.setOnClickListener(v -> finish());
        }

        // Cấu hình RecyclerView
        adminFlashSaleRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        // Tải danh sách Flash Sale ban đầu
        loadFlashSales();

        // Xử lý nút "Thêm Flash Sale"
        if (addFlashSaleButton != null) {
            addFlashSaleButton.setOnClickListener(v -> {
                // CHUYỂN ĐẾN ADMIN_CATEGORY_MANAGEMENT_ACTIVITY ĐỂ CHỌN SẢN PHẨM
                Intent intent = new Intent(AdminFlashSaleManagementActivity.this, AdminCategoryManagementActivity.class);
                intent.putExtra("adminUserId", adminUserId);
                intent.putExtra("fromFlashSale", true); // <-- Đánh dấu là đến từ luồng Flash Sale
                startActivity(intent);
            });
        }
    }

    private void loadFlashSales() {
        List<FlashSale> flashSales = flashSaleDAO.getAllFlashSales(); // Lấy tất cả Flash Sale

        if (flashSales.isEmpty()) {
            emptyFlashSaleMessage.setVisibility(View.VISIBLE);
            adminFlashSaleRecyclerView.setVisibility(View.GONE);
        } else {
            emptyFlashSaleMessage.setVisibility(View.GONE);
            adminFlashSaleRecyclerView.setVisibility(View.VISIBLE);
        }

        adapter = new AdminFlashSaleAdapter(this, flashSales, this); // Truyền 'this' làm listener
        adminFlashSaleRecyclerView.setAdapter(adapter);
    }

    @Override
    public void onFlashSaleDeleted() {
        loadFlashSales(); // Khi một Flash Sale bị xóa, tải lại danh sách
    }

    @Override
    public void onFlashSaleEdited(int flashSaleId) {
        // Chuyển đến màn hình chỉnh sửa chi tiết Flash Sale
        Toast.makeText(this, "Chỉnh sửa Flash Sale ID: " + flashSaleId, Toast.LENGTH_SHORT).show();
        Intent intent = new Intent(AdminFlashSaleManagementActivity.this, AdminFlashSaleDetailActivity.class);
        intent.putExtra("flashSaleId", flashSaleId);
        intent.putExtra("isAddingNew", false); // Đánh dấu là đang chỉnh sửa
        startActivity(intent);
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadFlashSales(); // Tải lại danh sách mỗi khi Activity được resumed (sau khi thêm/sửa/xóa xong)
    }
}