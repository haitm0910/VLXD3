// File: OrderDetailActivity.java

package com.example.vlxd3;

import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.util.Log; // Thêm import này

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.vlxd3.dao.OrderDAO;
import com.example.vlxd3.model.Order;
import com.example.vlxd3.model.OrderDetail;

import java.util.List;
import java.util.Locale;

public class OrderDetailActivity extends AppCompatActivity {

    private TextView orderStatusTextView;
    private TextView customerNameTextView, customerPhoneTextView, customerAddressTextView;
    private RecyclerView orderItemsRecyclerView;
    private TextView orderIdTextView, paymentMethodTextView, orderDateTextView, orderCompletionDateTextView;
    private OrderDAO orderDAO;
    private int orderId;
    private ImageView backButton; // Ánh xạ nút back

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order_detail);

        // Ánh xạ Views
        orderStatusTextView = findViewById(R.id.tv_detail_delivery_status);
        customerNameTextView = findViewById(R.id.tv_detail_customer_name);
        customerPhoneTextView = findViewById(R.id.tv_detail_customer_phone);
        customerAddressTextView = findViewById(R.id.tv_detail_customer_address);
        orderItemsRecyclerView = findViewById(R.id.rv_order_items_detail);
        orderIdTextView = findViewById(R.id.tv_detail_order_id);
        paymentMethodTextView = findViewById(R.id.tv_detail_payment_method);
        orderDateTextView = findViewById(R.id.tv_detail_order_date);
        orderCompletionDateTextView = findViewById(R.id.tv_detail_order_completion_date);

        // Xử lý nút back
        backButton = findViewById(R.id.iv_back_arrow_detail); // Ánh xạ nút back
        if (backButton != null) {
            backButton.setOnClickListener(v -> finish());
        }

        // Khởi tạo DAO
        orderDAO = new OrderDAO(this);

        // Lấy orderId từ Intent
        orderId = getIntent().getIntExtra("orderId", -1);
        Log.d("OrderDetailActivity", "Received Order ID: " + orderId); // Log để kiểm tra orderId

        if (orderId == -1) {
            Toast.makeText(this, "Không tìm thấy thông tin đơn hàng.", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        // Cấu hình RecyclerView
        orderItemsRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        // Tải và hiển thị chi tiết đơn hàng
        loadOrderDetail();
    }

    private void loadOrderDetail() {
        Order order = orderDAO.getOrderById(orderId);
        if (order != null) {
            Log.d("OrderDetailActivity", "Order found: " + order.getId() + ", Status: " + order.getStatus());

            orderStatusTextView.setText(order.getStatus());
            customerNameTextView.setText(order.getCustomerName());
            customerPhoneTextView.setText(order.getCustomerPhone());
            customerAddressTextView.setText(order.getCustomerAddress());

            orderIdTextView.setText(String.valueOf(order.getId()));
            paymentMethodTextView.setText(order.getPaymentMethod());
            orderDateTextView.setText(order.getOrderDate());
            orderCompletionDateTextView.setText("Chưa xác định"); // Hoặc tính toán nếu có trường phù hợp

            List<OrderDetail> orderDetails = orderDAO.getOrderDetailsByOrderId(orderId);
            Log.d("OrderDetailActivity", "Order Details size: " + orderDetails.size()); // Log số lượng chi tiết đơn hàng

            OrderDetailAdapter adapter = new OrderDetailAdapter(this, orderDetails);
            orderItemsRecyclerView.setAdapter(adapter);

        } else {
            Toast.makeText(this, "Không thể tải chi tiết đơn hàng.", Toast.LENGTH_SHORT).show();
            Log.e("OrderDetailActivity", "Order with ID " + orderId + " not found in database.");
            finish();
        }
    }
}