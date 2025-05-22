// File: OrderAdapter.java

package com.example.vlxd3;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.vlxd3.model.Order;

import java.util.List;
import java.util.Locale;

public class OrderAdapter extends RecyclerView.Adapter<OrderAdapter.OrderViewHolder> {

    private Context context;
    private List<Order> orderList;

    public OrderAdapter(Context context, List<Order> orderList) {
        this.context = context;
        this.orderList = orderList;
    }

    @NonNull
    @Override
    public OrderViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.list_order, parent, false);
        return new OrderViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull OrderViewHolder holder, int position) {
        Order order = orderList.get(position);

        holder.orderIdTextView.setText(String.valueOf(order.getId()));
        holder.orderDateTextView.setText(order.getOrderDate());
        holder.totalAmountTextView.setText(String.format(Locale.getDefault(), "%,.0f đ", order.getTotalAmount()));
        holder.orderStatusTextView.setText(order.getStatus());

        // THIẾT LẬP LISTENER CHO NÚT "XEM CHI TIẾT"
        holder.viewDetailsButton.setOnClickListener(v -> {
            Intent intent = new Intent(context, OrderDetailActivity.class); // Khởi tạo Intent đến OrderDetailActivity
            intent.putExtra("orderId", order.getId()); // Truyền orderId của đơn hàng
            context.startActivity(intent); // Khởi chạy Activity
        });
    }

    @Override
    public int getItemCount() {
        return orderList.size();
    }

    public static class OrderViewHolder extends RecyclerView.ViewHolder {
        TextView orderIdTextView, orderDateTextView, totalAmountTextView, orderStatusTextView;
        Button viewDetailsButton;

        public OrderViewHolder(@NonNull View itemView) {
            super(itemView);
            orderIdTextView = itemView.findViewById(R.id.tv_order_id);
            orderDateTextView = itemView.findViewById(R.id.tv_order_date);
            totalAmountTextView = itemView.findViewById(R.id.tv_total_amount);
            orderStatusTextView = itemView.findViewById(R.id.tv_order_status);
            viewDetailsButton = itemView.findViewById(R.id.btn_view_order_details);
        }
    }
}