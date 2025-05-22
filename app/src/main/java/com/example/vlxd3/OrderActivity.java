// File: OrderActivity.java (tên file của bạn)

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

import com.example.vlxd3.dao.OrderDAO;
import com.example.vlxd3.model.Order;

import java.util.List;

public class OrderActivity extends AppCompatActivity { // <-- Đã sửa tên lớp ở đây

    private RecyclerView orderListRecyclerView;
    private TextView emptyOrdersMessage;
    private OrderDAO orderDAO;
    private OrderAdapter adapter;
    private int userId;
    private ImageView backButtonOrder; // Ánh xạ nút back

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order); // Đảm bảo layout này tồn tại

        // Ánh xạ Views
        orderListRecyclerView = findViewById(R.id.rv_order_list);
        emptyOrdersMessage = findViewById(R.id.empty_orders_message);
        backButtonOrder = findViewById(R.id.backButtonOrder); // Ánh xạ nút back

        // Khởi tạo DAO
        orderDAO = new OrderDAO(this);

        // Nhận userId
        userId = getIntent().getIntExtra("userId", -1);
        if (userId == -1) {
            Toast.makeText(this, "Không tìm thấy thông tin người dùng. Vui lòng đăng nhập lại.", Toast.LENGTH_LONG).show();
            Intent loginIntent = new Intent(this, LoginActivity.class);
            startActivity(loginIntent);
            finish();
            return;
        }

        // Cấu hình RecyclerView
        orderListRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        // Tải và hiển thị dữ liệu đơn hàng
        loadOrders();

        // Xử lý nút back
        if (backButtonOrder != null) {
            backButtonOrder.setOnClickListener(v -> finish());
        }
    }

    private void loadOrders() {
        List<Order> orders = orderDAO.getOrdersByUserId(userId);

        if (orders.isEmpty()) {
            emptyOrdersMessage.setVisibility(View.VISIBLE);
            orderListRecyclerView.setVisibility(View.GONE);
        } else {
            emptyOrdersMessage.setVisibility(View.GONE);
            orderListRecyclerView.setVisibility(View.VISIBLE);
            adapter = new OrderAdapter(this, orders);
            orderListRecyclerView.setAdapter(adapter);
        }
    }
}