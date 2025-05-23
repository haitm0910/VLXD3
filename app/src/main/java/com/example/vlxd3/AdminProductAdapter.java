// File: AdminProductAdapter.java

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

import com.example.vlxd3.dao.ProductDAO;
import com.example.vlxd3.dao.FlashSaleDAO;
import com.example.vlxd3.model.Product;
import com.example.vlxd3.model.FlashSale;

import java.util.List;
import java.util.Locale;

public class AdminProductAdapter extends RecyclerView.Adapter<AdminProductAdapter.AdminProductViewHolder> {

    private Context context;
    private List<Product> productList;
    private ProductDAO productDAO;
    private FlashSaleDAO flashSaleDAO;
    private OnProductActionListener listener;
    private boolean fromFlashSaleContext; // <-- Biến mới để biết nguồn gọi

    // Định nghĩa interface để thông báo khi có hành động
    public interface OnProductActionListener {
        void onProductDeleted();
        void onProductEdited(int productId);
        void onProductAddToFlashSale(int productId); // <-- Phương thức mới
    }

    // Cập nhật constructor để nhận cờ fromFlashSaleContext
    public AdminProductAdapter(Context context, List<Product> productList, OnProductActionListener listener, boolean fromFlashSaleContext) { // <-- ĐÃ SỬA Ở ĐÂY
        this.context = context;
        this.productList = productList;
        this.productDAO = new ProductDAO(context);
        this.flashSaleDAO = new FlashSaleDAO(context);
        this.listener = listener;
        this.fromFlashSaleContext = fromFlashSaleContext; // <-- Gán cờ
    }

    @NonNull
    @Override
    public AdminProductViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.list_admin_product_item, parent, false);
        return new AdminProductViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull AdminProductViewHolder holder, int position) {
        Product product = productList.get(position);
        holder.productName.setText(product.getName());
        holder.productStock.setText("SL tồn: " + product.getStock());

        // Kiểm tra tình trạng Flash Sale của sản phẩm
        FlashSale flashSale = flashSaleDAO.getFlashSaleByProductId(product.getId());

        if (flashSale != null) {
            // Có flash sale
            holder.productOriginalPrice.setVisibility(View.VISIBLE);
            String originalPriceFormatted = String.format(Locale.getDefault(), "%,.0f đ", product.getPrice());
            holder.productOriginalPrice.setText(originalPriceFormatted);
            holder.productOriginalPrice.setPaintFlags(holder.productOriginalPrice.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
            holder.productOriginalPrice.setTextColor(Color.GRAY);

            String salePriceFormatted = String.format(Locale.getDefault(), "%,.0f đ", flashSale.getSalePrice());
            holder.productPrice.setText(salePriceFormatted);
            holder.productPrice.setTextColor(Color.RED);
            holder.productPrice.setTypeface(null, Typeface.BOLD);
        } else {
            // Không có flash sale
            holder.productOriginalPrice.setVisibility(View.GONE);
            String normalPriceFormatted = String.format(Locale.getDefault(), "%,.0f đ", product.getPrice());
            holder.productPrice.setText(normalPriceFormatted);
            holder.productPrice.setTextColor(Color.BLACK);
            holder.productPrice.setTypeface(null, Typeface.NORMAL);
        }

        // Xử lý hiển thị ảnh sản phẩm
        if (product.getImage() != null && !product.getImage().isEmpty()) {
            int imageResId = context.getResources().getIdentifier(product.getImage(), "drawable", context.getPackageName());
            if (imageResId != 0) {
                holder.productImage.setImageResource(imageResId);
            } else {
                holder.productImage.setImageResource(R.drawable.logo);
            }
        } else {
            holder.productImage.setImageResource(R.drawable.logo);
        }

        // --- QUẢN LÝ HIỂN THỊ NÚT DỰA TRÊN CONTEXT (FROM FLASH SALE HAY KHÔNG) ---
        if (fromFlashSaleContext) {
            // Nếu đến từ luồng Flash Sale: hiển thị nút Add/Remove Flash Sale
            holder.editButton.setVisibility(View.GONE); // Ẩn nút Edit
            holder.deleteButton.setVisibility(View.GONE); // Ẩn nút Delete
            holder.flashSaleActionButton.setVisibility(View.VISIBLE); // Hiển thị nút hành động Flash Sale

            if (flashSale != null) { // Nếu sản phẩm đang sale
                holder.flashSaleActionButton.setImageResource(R.drawable.remove); // Dấu trừ
                holder.flashSaleActionButton.setOnClickListener(v -> {
                    // Ngừng Flash Sale (xóa bản ghi Flash Sale)
                    new AlertDialog.Builder(context)
                            .setTitle("Xác nhận ngừng Flash Sale")
                            .setMessage("Bạn có chắc muốn ngừng chương trình Flash Sale cho sản phẩm '" + product.getName() + "'?")
                            .setPositiveButton("Ngừng Sale", (dialog, which) -> {
                                boolean success = flashSaleDAO.deleteFlashSale(flashSale.getId());
                                if (success) {
                                    Toast.makeText(context, "Đã ngừng Flash Sale cho sản phẩm: " + product.getName(), Toast.LENGTH_SHORT).show();
                                    if (listener != null) {
                                        listener.onProductDeleted(); // Kích hoạt sự kiện làm mới
                                    }
                                } else {
                                    Toast.makeText(context, "Ngừng Flash Sale thất bại!", Toast.LENGTH_SHORT).show();
                                }
                            })
                            .setNegativeButton("Hủy", null)
                            .show();
                });
            } else { // Nếu sản phẩm không sale
                holder.flashSaleActionButton.setImageResource(R.drawable.add); // Dấu cộng
                holder.flashSaleActionButton.setOnClickListener(v -> {
                    // Thêm vào Flash Sale
                    if (listener != null) {
                        listener.onProductAddToFlashSale(product.getId()); // Gọi phương thức mới trong Activity
                    }
                });
            }

        } else {
            // Nếu không phải từ luồng Flash Sale: hiển thị nút Edit/Delete sản phẩm thông thường
            holder.editButton.setVisibility(View.VISIBLE);
            holder.deleteButton.setVisibility(View.VISIBLE);
            holder.flashSaleActionButton.setVisibility(View.GONE); // Ẩn nút hành động Flash Sale

            // Xử lý nút xóa sản phẩm
            holder.deleteButton.setOnClickListener(v -> {
                new AlertDialog.Builder(context)
                        .setTitle("Xác nhận xóa sản phẩm")
                        .setMessage("Bạn có chắc muốn xóa sản phẩm '" + product.getName() + "'?")
                        .setPositiveButton("Xóa", (dialog, which) -> {
                            boolean success = productDAO.deleteProduct(product.getId());
                            if (success) {
                                Toast.makeText(context, "Đã xóa sản phẩm: " + product.getName(), Toast.LENGTH_SHORT).show();
                                if (listener != null) {
                                    listener.onProductDeleted();
                                }
                            } else {
                                Toast.makeText(context, "Xóa sản phẩm thất bại!", Toast.LENGTH_SHORT).show();
                            }
                        })
                        .setNegativeButton("Hủy", null)
                        .show();
            });

            // Xử lý nút chỉnh sửa sản phẩm
            holder.editButton.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onProductEdited(product.getId());
                }
            });
        }
        // --- KẾT THÚC QUẢN LÝ NÚT ---
    }

    @Override
    public int getItemCount() {
        return productList.size();
    }

    public static class AdminProductViewHolder extends RecyclerView.ViewHolder {
        ImageView productImage;
        TextView productName;
        TextView productPrice;
        TextView productOriginalPrice;
        TextView productStock;
        ImageView deleteButton;
        ImageView editButton;
        ImageView flashSaleActionButton; // <-- ImageView mới cho nút hành động Flash Sale

        public AdminProductViewHolder(@NonNull View itemView) {
            super(itemView);
            productImage = itemView.findViewById(R.id.admin_product_image);
            productName = itemView.findViewById(R.id.admin_product_name);
            productPrice = itemView.findViewById(R.id.admin_product_price);
            productOriginalPrice = itemView.findViewById(R.id.admin_product_original_price);
            productStock = itemView.findViewById(R.id.admin_product_stock);
            deleteButton = itemView.findViewById(R.id.deleteProductButton);
            editButton = itemView.findViewById(R.id.editProductButton);
            flashSaleActionButton = itemView.findViewById(R.id.flashSaleActionButton); // <-- Ánh xạ nút mới
        }
    }
}