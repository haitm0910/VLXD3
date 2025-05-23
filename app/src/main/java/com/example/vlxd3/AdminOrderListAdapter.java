// File: AdminOrderListAdapter.java

package com.example.vlxd3;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color; // Import Color
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.vlxd3.dao.OrderDAO;
import com.example.vlxd3.dao.UserDAO; // Import UserDAO
import com.example.vlxd3.model.Order;
import com.example.vlxd3.model.User; // Import User

import java.util.List;
import java.util.Locale;

public class AdminOrderListAdapter extends RecyclerView.Adapter<AdminOrderListAdapter.AdminOrderViewHolder> {

    private Context context;
    private List<Order> orderList;
    private OrderDAO orderDAO;
    private UserDAO userDAO; // Khai báo UserDAO để lấy thông tin người dùng
    private OnOrderStatusChangeListener statusChangeListener; // Interface để thông báo Activity

    // Định nghĩa interface để thông báo khi trạng thái thay đổi
    public interface OnOrderStatusChangeListener {
        void onOrderStatusChanged();
    }

    public AdminOrderListAdapter(Context context, List<Order> orderList, OnOrderStatusChangeListener listener) {
        this.context = context;
        this.orderList = orderList;
        this.orderDAO = new OrderDAO(context);
        this.userDAO = new UserDAO(context); // Khởi tạo UserDAO
        this.statusChangeListener = listener;
    }

    @NonNull
    @Override
    public AdminOrderViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.list_admin_order_item, parent, false);
        return new AdminOrderViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull AdminOrderViewHolder holder, int position) {
        Order order = orderList.get(position);

        holder.orderIdTextView.setText(String.valueOf(order.getId()));
        holder.orderDateTextView.setText(order.getOrderDate());
        holder.totalAmountTextView.setText(String.format(Locale.getDefault(), "%,.0f đ", order.getTotalAmount()));
        holder.orderStatusTextView.setText(order.getStatus());

        // Lấy thông tin người dùng để hiển thị tên và số điện thoại
        User customer = userDAO.getUserById(order.getUserId());
        if (customer != null) {
            holder.customerNameTextView.setText(customer.getFullName());
            holder.customerPhoneTextView.setText(customer.getPhone());
        } else {
            holder.customerNameTextView.setText("Người dùng không xác định");
            holder.customerPhoneTextView.setText("");
        }

        // Đặt màu trạng thái
        switch (order.getStatus()) {
            case "Đang chờ xác nhận":
                holder.orderStatusTextView.setTextColor(Color.parseColor("#FF9800")); // Màu cam
                holder.btnChangeStatusToShipping.setVisibility(View.VISIBLE); // Chỉ hiển thị nút khi trạng thái là "Chờ xác nhận"
                break;
            case "Đang vận chuyển":
                holder.orderStatusTextView.setTextColor(Color.parseColor("#2196F3")); // Màu xanh dương
                holder.btnChangeStatusToShipping.setVisibility(View.GONE); // Ẩn nút nếu đã đang vận chuyển
                break;
            case "Đã giao":
                holder.orderStatusTextView.setTextColor(Color.parseColor("#4CAF50")); // Màu xanh lá
                holder.btnChangeStatusToShipping.setVisibility(View.GONE);
                break;
            case "Đã hủy":
                holder.orderStatusTextView.setTextColor(Color.parseColor("#F44336")); // Màu đỏ
                holder.btnChangeStatusToShipping.setVisibility(View.GONE);
                break;
            default:
                holder.orderStatusTextView.setTextColor(Color.BLACK);
                holder.btnChangeStatusToShipping.setVisibility(View.GONE);
                break;
        }


        // Xử lý nút "Xem chi tiết"
        holder.btnViewDetails.setOnClickListener(v -> {
            Intent intent = new Intent(context, OrderDetailActivity.class);
            intent.putExtra("orderId", order.getId());
            // Có thể truyền thêm thông tin người đặt nếu OrderDetailActivity cần
            context.startActivity(intent);
        });

        // Xử lý nút "Chuyển đang giao"
        holder.btnChangeStatusToShipping.setOnClickListener(v -> {
            boolean success = orderDAO.updateOrderStatus(order.getId(), "Đang vận chuyển");
            if (success) {
                Toast.makeText(context, "Đã chuyển trạng thái đơn hàng " + order.getId() + " thành Đang vận chuyển", Toast.LENGTH_SHORT).show();
                if (statusChangeListener != null) {
                    statusChangeListener.onOrderStatusChanged(); // Yêu cầu Activity làm mới danh sách
                }
            } else {
                Toast.makeText(context, "Không thể cập nhật trạng thái đơn hàng " + order.getId(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public int getItemCount() {
        return orderList.size();
    }

    public static class AdminOrderViewHolder extends RecyclerView.ViewHolder {
        TextView orderIdTextView, orderDateTextView, customerNameTextView, customerPhoneTextView, orderStatusTextView, totalAmountTextView;
        Button btnViewDetails, btnChangeStatusToShipping;

        public AdminOrderViewHolder(@NonNull View itemView) {
            super(itemView);
            orderIdTextView = itemView.findViewById(R.id.tv_admin_order_id);
            orderDateTextView = itemView.findViewById(R.id.tv_admin_order_date);
            customerNameTextView = itemView.findViewById(R.id.tv_admin_customer_name);
            customerPhoneTextView = itemView.findViewById(R.id.tv_admin_customer_phone);
            orderStatusTextView = itemView.findViewById(R.id.tv_admin_order_status);
            totalAmountTextView = itemView.findViewById(R.id.tv_admin_total_amount);
            btnViewDetails = itemView.findViewById(R.id.btn_admin_view_details);
            btnChangeStatusToShipping = itemView.findViewById(R.id.btn_admin_change_status_to_shipping);
        }
    }
}