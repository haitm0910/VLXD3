// File: OrderDetailAdapter.java

package com.example.vlxd3;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.vlxd3.dao.ProductDAO;
import com.example.vlxd3.model.OrderDetail;
import com.example.vlxd3.model.Product;

import java.util.List;
import java.util.Locale;

public class OrderDetailAdapter extends RecyclerView.Adapter<OrderDetailAdapter.OrderDetailViewHolder> {

    private Context context;
    private List<OrderDetail> orderDetailList;
    private ProductDAO productDAO; // Để lấy thông tin sản phẩm

    public OrderDetailAdapter(Context context, List<OrderDetail> orderDetailList) {
        this.context = context;
        this.orderDetailList = orderDetailList;
        this.productDAO = new ProductDAO(context);
    }

    @NonNull
    @Override
    public OrderDetailViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_order_product, parent, false);
        return new OrderDetailViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull OrderDetailViewHolder holder, int position) {
        OrderDetail detail = orderDetailList.get(position);
        Product product = productDAO.getProductById(detail.getProductId()); // Lấy thông tin sản phẩm

        if (product != null) {
            holder.productNameTextView.setText(product.getName());
            holder.productQuantityTextView.setText("SL: " + detail.getQuantity());
            holder.unitPriceTextView.setText(String.format(Locale.getDefault(), "%,.0f đ/sản phẩm", detail.getUnitPrice()));
            holder.totalItemPriceTextView.setText(String.format(Locale.getDefault(), "%,.0f đ", detail.getUnitPrice() * detail.getQuantity()));

            // TODO: Load ảnh sản phẩm (tương tự như trong ProductAdapter hoặc FlashSaleAdapter)
            if (product.getImage() != null && !product.getImage().isEmpty()) {
                int imageResId = context.getResources().getIdentifier(product.getImage(), "drawable", context.getPackageName());
                if (imageResId != 0) {
                    holder.productImageView.setImageResource(imageResId);
                } else {
                    holder.productImageView.setImageResource(R.drawable.logo); // Fallback
                }
            } else {
                holder.productImageView.setImageResource(R.drawable.logo); // Mặc định
            }

        } else {
            holder.productNameTextView.setText("Sản phẩm không tồn tại");
            holder.productQuantityTextView.setText("SL: " + detail.getQuantity());
            holder.unitPriceTextView.setText("N/A");
            holder.totalItemPriceTextView.setText("0 đ");
            holder.productImageView.setImageResource(R.drawable.logo); // Fallback
        }
    }

    @Override
    public int getItemCount() {
        return orderDetailList.size();
    }

    public static class OrderDetailViewHolder extends RecyclerView.ViewHolder {
        ImageView productImageView;
        TextView productNameTextView, productQuantityTextView, unitPriceTextView, totalItemPriceTextView;

        public OrderDetailViewHolder(@NonNull View itemView) {
            super(itemView);
            productImageView = itemView.findViewById(R.id.order_product_image);
            productNameTextView = itemView.findViewById(R.id.order_product_name);
            productQuantityTextView = itemView.findViewById(R.id.order_product_quantity);
            unitPriceTextView = itemView.findViewById(R.id.order_product_unit_price);
            totalItemPriceTextView = itemView.findViewById(R.id.order_product_total_item_price);
        }
    }
}