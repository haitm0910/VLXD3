// File: AdminCategoryManagementActivity.java

package com.example.vlxd3;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.vlxd3.dao.CategoryDAO;
import com.example.vlxd3.model.Category;

import java.util.List;

public class AdminCategoryManagementActivity extends AppCompatActivity implements AdminCategoryAdapter.OnCategoryActionListener {

    private RecyclerView adminCategoryRecyclerView;
    private TextView emptyCategoryMessage;
    private ImageView backButton;
    private ImageView addCategoryButton;

    private CategoryDAO categoryDAO;
    private AdminCategoryAdapter adapter;

    private int adminUserId;
    private boolean fromFlashSale = false; // <-- Biến mới để kiểm tra nguồn gọi

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_category_management);

        // Ánh xạ Views
        adminCategoryRecyclerView = findViewById(R.id.rv_admin_category_list);
        emptyCategoryMessage = findViewById(R.id.empty_category_message);
        backButton = findViewById(R.id.backButtonAdminCategory);
        addCategoryButton = findViewById(R.id.addCategoryButton);

        adminUserId = getIntent().getIntExtra("userId", -1);
        fromFlashSale = getIntent().getBooleanExtra("fromFlashSale", false); // <-- Lấy cờ từ Intent

        // Khởi tạo DAO
        categoryDAO = new CategoryDAO(this);

        // Xử lý nút back
        if (backButton != null) {
            backButton.setOnClickListener(v -> finish());
        }

        // Cấu hình RecyclerView
        adminCategoryRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        // Tải danh mục ban đầu
        loadCategories();

        // Xử lý nút "Thêm danh mục"
        if (addCategoryButton != null) {
            addCategoryButton.setOnClickListener(v -> showAddCategoryDialog());
        }
    }

    private void loadCategories() {
        List<Category> categories = categoryDAO.getAllCategories();

        if (categories.isEmpty()) {
            emptyCategoryMessage.setVisibility(View.VISIBLE);
            adminCategoryRecyclerView.setVisibility(View.GONE);
        } else {
            emptyCategoryMessage.setVisibility(View.GONE);
            adminCategoryRecyclerView.setVisibility(View.VISIBLE);
        }

        adapter = new AdminCategoryAdapter(this, categories, this);
        adminCategoryRecyclerView.setAdapter(adapter);
    }

    private void showAddCategoryDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Thêm danh mục mới");

        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_add_category, null);
        EditText categoryNameEt = dialogView.findViewById(R.id.et_dialog_category_name);
        EditText categoryImageEt = dialogView.findViewById(R.id.et_dialog_category_image);

        builder.setView(dialogView);

        builder.setPositiveButton("Thêm", (dialog, which) -> {
            String name = categoryNameEt.getText().toString().trim();
            String image = categoryImageEt.getText().toString().trim();

            if (name.isEmpty()) {
                Toast.makeText(AdminCategoryManagementActivity.this, "Tên danh mục không được trống!", Toast.LENGTH_SHORT).show();
                return;
            }

            Category newCategory = new Category(name, image);
            long id = categoryDAO.addCategory(newCategory);

            if (id != -1) {
                Toast.makeText(AdminCategoryManagementActivity.this, "Đã thêm danh mục: " + name, Toast.LENGTH_SHORT).show();
                loadCategories(); // Làm mới danh sách
            } else {
                Toast.makeText(AdminCategoryManagementActivity.this, "Thêm danh mục thất bại!", Toast.LENGTH_SHORT).show();
            }
        });

        builder.setNegativeButton("Hủy", (dialog, which) -> dialog.cancel());
        builder.show();
    }

    @Override
    public void onCategoryDeleted() {
        loadCategories(); // Khi một danh mục bị xóa, tải lại danh sách
    }

    @Override
    public void onCategoryClicked(int categoryId, String categoryName) {
        // KIỂM TRA CỜ fromFlashSale
        Intent intent = new Intent(AdminCategoryManagementActivity.this, AdminProductManagementActivity.class);
        intent.putExtra("categoryId", categoryId);
        intent.putExtra("categoryName", categoryName);
        intent.putExtra("adminUserId", adminUserId);

        if (fromFlashSale) { // <-- Nếu đến từ luồng Flash Sale
            intent.putExtra("fromFlashSale", true); // Truyền cờ này sang AdminProductManagementActivity
            Toast.makeText(this, "Chọn sản phẩm để tạo Flash Sale trong danh mục: " + categoryName, Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "Quản lý sản phẩm của danh mục: " + categoryName, Toast.LENGTH_SHORT).show();
        }
        startActivity(intent);
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadCategories(); // Tải lại danh mục mỗi khi Activity được resumed
    }
}