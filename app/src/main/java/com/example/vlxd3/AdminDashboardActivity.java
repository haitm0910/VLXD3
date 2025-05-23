// File: AdminDashboardActivity.java

package com.example.vlxd3;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

public class AdminDashboardActivity extends AppCompatActivity {

    private ImageView logoutButton;
    private CardView manageOrdersCard, manageProductsCard, manageFlashSalesCard;
    private int adminUserId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_dashboard);

        logoutButton = findViewById(R.id.iv_admin_logout);
        manageOrdersCard = findViewById(R.id.card_manage_orders);
        manageProductsCard = findViewById(R.id.card_manage_products);
        manageFlashSalesCard = findViewById(R.id.card_manage_flash_sales);

        adminUserId = getIntent().getIntExtra("userId", -1);

        if (logoutButton != null) {
            logoutButton.setOnClickListener(v -> {
                Toast.makeText(AdminDashboardActivity.this, "Đăng xuất tài khoản Admin!", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(AdminDashboardActivity.this, LoginActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                finish();
            });
        }

        // Xử lý quản lý đơn hàng
        if (manageOrdersCard != null) {
            manageOrdersCard.setOnClickListener(v -> {
                Intent intent = new Intent(AdminDashboardActivity.this, AdminOrderListActivity.class);
                intent.putExtra("userId", adminUserId);
                startActivity(intent);
            });
        }

        // Xử lý quản lý sản phẩm
        if (manageProductsCard != null) {
            manageProductsCard.setOnClickListener(v -> {
                Intent intent = new Intent(AdminDashboardActivity.this, AdminCategoryManagementActivity.class);
                intent.putExtra("userId", adminUserId);
                startActivity(intent);
            });
        }

        // Xử lý quản lý Flash Sale
        if (manageFlashSalesCard != null) {
            manageFlashSalesCard.setOnClickListener(v -> {
                Intent intent = new Intent(AdminDashboardActivity.this, AdminFlashSaleManagementActivity.class);
                intent.putExtra("userId", adminUserId);
                startActivity(intent);
            });
        }
    }
}