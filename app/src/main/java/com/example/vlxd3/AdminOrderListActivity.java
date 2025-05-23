// File: AdminOrderListActivity.java

package com.example.vlxd3;

import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.vlxd3.dao.OrderDAO;
import com.example.vlxd3.model.Order;

import java.util.List;

public class AdminOrderListActivity extends AppCompatActivity implements AdminOrderListAdapter.OnOrderStatusChangeListener { // Implement interface

    private RecyclerView adminOrderListRecyclerView;
    private TextView emptyAdminOrdersMessage;
    private Spinner orderStatusSpinner;
    private Button filterOrdersButton;
    private ImageView backButton;

    private OrderDAO orderDAO;
    private AdminOrderListAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_order_list); // Đảm bảo layout này tồn tại

        // Ánh xạ Views
        adminOrderListRecyclerView = findViewById(R.id.rv_admin_order_list);
        emptyAdminOrdersMessage = findViewById(R.id.empty_admin_orders_message);
        orderStatusSpinner = findViewById(R.id.spinnerOrderStatus);
        filterOrdersButton = findViewById(R.id.btnFilterOrders);
        backButton = findViewById(R.id.backButtonAdminOrderList);

        // Khởi tạo DAO
        orderDAO = new OrderDAO(this);

        // Xử lý nút back
        if (backButton != null) {
            backButton.setOnClickListener(v -> finish());
        }

        // Cấu hình Spinner
        ArrayAdapter<CharSequence> spinnerAdapter = ArrayAdapter.createFromResource(
                this, R.array.order_status_options, android.R.layout.simple_spinner_item);
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        orderStatusSpinner.setAdapter(spinnerAdapter);

        // Cấu hình RecyclerView
        adminOrderListRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        // Tải đơn hàng ban đầu (chỉ các đơn "Đang chờ xác nhận")
        loadOrders("Đang chờ xác nhận"); // Mặc định chỉ hiển thị đơn chờ xác nhận

        // Xử lý nút lọc
        if (filterOrdersButton != null) {
            filterOrdersButton.setOnClickListener(v -> {
                String selectedStatus = orderStatusSpinner.getSelectedItem().toString();
                if (selectedStatus.equals("Tất cả")) {
                    loadOrders(null); // Truyền null để lấy tất cả
                } else {
                    loadOrders(selectedStatus);
                }
            });
        }
    }

    // Phương thức tải đơn hàng dựa trên trạng thái
    private void loadOrders(String status) {
        List<Order> orders;
        if (status == null || status.equals("Tất cả")) {
            orders = orderDAO.getAllOrders(); // Lấy tất cả nếu status là null hoặc "Tất cả"
        } else {
            orders = orderDAO.getOrdersByStatus(status); // Lấy theo trạng thái
        }

        if (orders.isEmpty()) {
            emptyAdminOrdersMessage.setVisibility(View.VISIBLE);
            adminOrderListRecyclerView.setVisibility(View.GONE);
        } else {
            emptyAdminOrdersMessage.setVisibility(View.GONE);
            adminOrderListRecyclerView.setVisibility(View.VISIBLE);
        }

        // Cập nhật Adapter (hoặc tạo mới nếu adapter là null)
        if (adapter == null) {
            adapter = new AdminOrderListAdapter(this, orders, this); // Truyền 'this' làm listener
            adminOrderListRecyclerView.setAdapter(adapter);
        } else {
            // Cập nhật danh sách trong Adapter và thông báo thay đổi
            adapter = new AdminOrderListAdapter(this, orders, this); // Re-initialize adapter for simplicity
            adminOrderListRecyclerView.setAdapter(adapter);
            // Một cách khác là tạo phương thức `setOrderList(List<Order> newOrders)` trong Adapter
            // adapter.setOrderList(orders);
            // adapter.notifyDataSetChanged();
        }
    }

    @Override
    public void onOrderStatusChanged() {
        // Khi trạng thái đơn hàng được cập nhật, tải lại danh sách đơn hàng hiện tại
        String selectedStatus = orderStatusSpinner.getSelectedItem().toString();
        if (selectedStatus.equals("Tất cả")) {
            loadOrders(null);
        } else {
            loadOrders(selectedStatus);
        }
    }
}