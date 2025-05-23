// File: AdminProductManagementActivity.java

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

import com.example.vlxd3.dao.ProductDAO;
import com.example.vlxd3.model.Product;

import java.util.List;

public class AdminProductManagementActivity extends AppCompatActivity implements AdminProductAdapter.OnProductActionListener {

    private RecyclerView adminProductRecyclerView;
    private TextView emptyProductMessage;
    private ImageView backButton;
    private ImageView addProductButton;
    private TextView categoryNameTextView;

    private ProductDAO productDAO;
    private AdminProductAdapter adapter;

    private int categoryId;
    private String categoryName;
    private int adminUserId;
    private boolean fromFlashSale = false; // <-- Biến mới để kiểm tra nguồn gọi

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_product_management);

        // Ánh xạ Views
        adminProductRecyclerView = findViewById(R.id.rv_admin_product_list);
        emptyProductMessage = findViewById(R.id.empty_product_message);
        backButton = findViewById(R.id.backButtonAdminProduct);
        addProductButton = findViewById(R.id.addProductButton);
        categoryNameTextView = findViewById(R.id.tv_admin_product_category_name);

        // Lấy categoryId, categoryName và cờ fromFlashSale từ Intent
        categoryId = getIntent().getIntExtra("categoryId", -1);
        categoryName = getIntent().getStringExtra("categoryName");
        adminUserId = getIntent().getIntExtra("userId", -1);
        fromFlashSale = getIntent().getBooleanExtra("fromFlashSale", false); // <-- Lấy cờ từ Intent

        if (categoryId == -1) {
            Toast.makeText(this, "Không tìm thấy danh mục để quản lý sản phẩm.", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        if (categoryName != null) {
            categoryNameTextView.setText("Sản phẩm: " + categoryName);
        } else {
            categoryNameTextView.setText("Sản phẩm theo danh mục");
        }

        // Ẩn nút "Thêm sản phẩm" nếu đến từ luồng Flash Sale (vì thêm Flash Sale được thực hiện qua item)
        if (fromFlashSale && addProductButton != null) {
            addProductButton.setVisibility(View.GONE);
        } else if (addProductButton != null) {
            addProductButton.setOnClickListener(v -> showAddProductActivity());
        }

        // Khởi tạo DAO
        productDAO = new ProductDAO(this);

        // Xử lý nút back
        if (backButton != null) {
            backButton.setOnClickListener(v -> finish());
        }

        // Cấu hình RecyclerView
        adminProductRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        // Tải sản phẩm ban đầu
        loadProducts();
    }

    private void loadProducts() {
        List<Product> products = productDAO.getProductsByCategory(categoryId);

        if (products.isEmpty()) {
            emptyProductMessage.setVisibility(View.VISIBLE);
            adminProductRecyclerView.setVisibility(View.GONE);
        } else {
            emptyProductMessage.setVisibility(View.GONE);
            adminProductRecyclerView.setVisibility(View.VISIBLE);
        }

        // Truyền cờ fromFlashSale cho Adapter
        adapter = new AdminProductAdapter(this, products, this, fromFlashSale); // <-- ĐÃ SỬA Ở ĐÂY
        adminProductRecyclerView.setAdapter(adapter);
    }

    private void showAddProductActivity() {
        Intent intent = new Intent(AdminProductManagementActivity.this, AdminProductDetailActivity.class);
        intent.putExtra("categoryId", categoryId);
        intent.putExtra("adminUserId", adminUserId);
        intent.putExtra("isAddingNew", true);
        startActivity(intent);
    }

    @Override
    public void onProductDeleted() {
        loadProducts();
    }

    @Override
    public void onProductEdited(int productId) {
        // Chuyển đến màn hình chỉnh sửa chi tiết sản phẩm
        Toast.makeText(this, "Chỉnh sửa sản phẩm ID: " + productId, Toast.LENGTH_SHORT).show();
        Intent intent = new Intent(AdminProductManagementActivity.this, AdminProductDetailActivity.class);
        intent.putExtra("productId", productId);
        intent.putExtra("categoryId", categoryId);
        intent.putExtra("adminUserId", adminUserId);
        intent.putExtra("isAddingNew", false);
        startActivity(intent);
    }

    // Phương thức mới: Khi nhấn nút '+' trên sản phẩm để thêm vào Flash Sale
    @Override
    public void onProductAddToFlashSale(int productId) {
        // Chuyển đến màn hình AdminFlashSaleDetailActivity để thiết lập Flash Sale
        Toast.makeText(this, "Tạo Flash Sale cho sản phẩm ID: " + productId, Toast.LENGTH_SHORT).show();
        Intent intent = new Intent(AdminProductManagementActivity.this, AdminFlashSaleDetailActivity.class);
        intent.putExtra("productId", productId); // Truyền ID sản phẩm
        intent.putExtra("isAddingNew", true); // Đánh dấu là đang thêm Flash Sale mới
        startActivity(intent);
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadProducts();
    }
}