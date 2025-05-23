// File: AdminFlashSaleAdapter.java

package com.example.vlxd3;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;

import com.example.vlxd3.dao.FlashSaleDAO;
import com.example.vlxd3.dao.ProductDAO; // Import ProductDAO để lấy tên sản phẩm
import com.example.vlxd3.model.FlashSale;
import com.example.vlxd3.model.Product; // Import Product

import java.util.List;
import java.util.Locale;

public class AdminFlashSaleAdapter extends RecyclerView.Adapter<AdminFlashSaleAdapter.AdminFlashSaleViewHolder> {

    private Context context;
    private List<FlashSale> flashSaleList;
    private FlashSaleDAO flashSaleDAO;
    private ProductDAO productDAO; // Khai báo ProductDAO
    private OnFlashSaleActionListener listener;

    // Định nghĩa interface để thông báo khi có hành động
    public interface OnFlashSaleActionListener {
        void onFlashSaleDeleted(); // Khi một Flash Sale bị xóa
        void onFlashSaleEdited(int flashSaleId); // Khi một Flash Sale được chỉnh sửa
    }

    public AdminFlashSaleAdapter(Context context, List<FlashSale> flashSaleList, OnFlashSaleActionListener listener) {
        this.context = context;
        this.flashSaleList = flashSaleList;
        this.flashSaleDAO = new FlashSaleDAO(context);
        this.productDAO = new ProductDAO(context); // Khởi tạo ProductDAO
        this.listener = listener;
    }

    @NonNull
    @Override
    public AdminFlashSaleViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.list_admin_flash_sale_item, parent, false);
        return new AdminFlashSaleViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull AdminFlashSaleViewHolder holder, int position) {
        FlashSale flashSale = flashSaleList.get(position);
        Product product = productDAO.getProductById(flashSale.getProductId()); // Lấy thông tin sản phẩm

        if (product != null) {
            holder.productName.setText(product.getName());
            holder.flashSaleStartDate.setText("Bắt đầu: " + flashSale.getStartDate());
            holder.flashSaleEndDate.setText("Kết thúc: " + flashSale.getEndDate());

            // Hiển thị giá gốc bị gạch ngang
            holder.productOriginalPrice.setVisibility(View.VISIBLE);
            String originalPriceFormatted = String.format(Locale.getDefault(), "%,.0f đ", product.getPrice());
            holder.productOriginalPrice.setText(originalPriceFormatted);
            holder.productOriginalPrice.setPaintFlags(holder.productOriginalPrice.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
            holder.productOriginalPrice.setTextColor(Color.GRAY);

            // Hiển thị giá đã giảm
            String salePriceFormatted = String.format(Locale.getDefault(), "%,.0f đ", flashSale.getSalePrice());
            holder.productSalePrice.setText(salePriceFormatted);
            holder.productSalePrice.setTextColor(Color.RED);
            holder.productSalePrice.setTypeface(null, Typeface.BOLD);

            // TODO: Xử lý hiển thị ảnh sản phẩm
            if (product.getImage() != null && !product.getImage().isEmpty()) {
                int imageResId = context.getResources().getIdentifier(product.getImage(), "drawable", context.getPackageName());
                if (imageResId != 0) {
                    holder.productImage.setImageResource(imageResId);
                } else {
                    holder.productImage.setImageResource(R.drawable.logo); // Fallback
                }
            } else {
                holder.productImage.setImageResource(R.drawable.logo); // Mặc định
            }

        } else {
            // Xử lý trường hợp không tìm thấy sản phẩm
            holder.productName.setText("Sản phẩm không tồn tại (ID: " + flashSale.getProductId() + ")");
            holder.productOriginalPrice.setVisibility(View.GONE);
            holder.productSalePrice.setText("N/A");
            holder.flashSaleStartDate.setText("");
            holder.flashSaleEndDate.setText("");
            holder.productImage.setImageResource(R.drawable.logo);
        }

        // Xử lý nút xóa Flash Sale
        holder.deleteButton.setOnClickListener(v -> {
            new AlertDialog.Builder(context)
                    .setTitle("Xác nhận xóa Flash Sale")
                    .setMessage("Bạn có chắc muốn xóa chương trình Flash Sale cho sản phẩm '" + product.getName() + "'?")
                    .setPositiveButton("Xóa", (dialog, which) -> {
                        boolean success = flashSaleDAO.deleteFlashSale(flashSale.getId());
                        if (success) {
                            Toast.makeText(context, "Đã xóa Flash Sale: " + product.getName(), Toast.LENGTH_SHORT).show();
                            if (listener != null) {
                                listener.onFlashSaleDeleted();
                            }
                        } else {
                            Toast.makeText(context, "Xóa Flash Sale thất bại!", Toast.LENGTH_SHORT).show();
                        }
                    })
                    .setNegativeButton("Hủy", null)
                    .show();
        });

        // Xử lý nút chỉnh sửa Flash Sale
        holder.editButton.setOnClickListener(v -> {
            if (listener != null) {
                listener.onFlashSaleEdited(flashSale.getId());
            }
        });
    }

    @Override
    public int getItemCount() {
        return flashSaleList.size();
    }

    public static class AdminFlashSaleViewHolder extends RecyclerView.ViewHolder {
        ImageView productImage;
        TextView productName;
        TextView productOriginalPrice;
        TextView productSalePrice;
        TextView flashSaleStartDate;
        TextView flashSaleEndDate;
        ImageView deleteButton;
        ImageView editButton;

        public AdminFlashSaleViewHolder(@NonNull View itemView) {
            super(itemView);
            productImage = itemView.findViewById(R.id.admin_flash_sale_product_image);
            productName = itemView.findViewById(R.id.admin_flash_sale_product_name);
            productOriginalPrice = itemView.findViewById(R.id.admin_flash_sale_original_price);
            productSalePrice = itemView.findViewById(R.id.admin_flash_sale_sale_price);
            flashSaleStartDate = itemView.findViewById(R.id.admin_flash_sale_start_date);
            flashSaleEndDate = itemView.findViewById(R.id.admin_flash_sale_end_date);
            deleteButton = itemView.findViewById(R.id.deleteFlashSaleButton);
            editButton = itemView.findViewById(R.id.editFlashSaleButton);
        }
    }
}