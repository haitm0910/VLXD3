// File: FlashSaleMainAdapter.java
package com.example.vlxd3;

import android.content.Context;
import android.content.Intent;
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
import androidx.recyclerview.widget.RecyclerView;

import com.example.vlxd3.dao.FlashSaleDAO;
import com.example.vlxd3.dao.ProductDAO;
import com.example.vlxd3.model.FlashSale;
import com.example.vlxd3.model.Product;

import java.util.List;
import java.util.Locale;

public class FlashSaleMainAdapter extends RecyclerView.Adapter<FlashSaleMainAdapter.FlashSaleViewHolder> {

    private Context context;
    private List<FlashSale> flashSaleList; // Danh sách các đối tượng FlashSale
    private int userId;
    private ProductDAO productDAO; // Để lấy thông tin Product
    private FlashSaleDAO flashSaleDAO; // Để kiểm tra flash sale (nếu cần logic ngày giờ ở đây)

    public FlashSaleMainAdapter(Context context, List<FlashSale> flashSaleList, int userId) {
        this.context = context;
        this.flashSaleList = flashSaleList;
        this.userId = userId;
        this.productDAO = new ProductDAO(context);
        this.flashSaleDAO = new FlashSaleDAO(context); // Khởi tạo FlashSaleDAO
    }

    @NonNull
    @Override
    public FlashSaleViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Sử dụng layout item cho flash sale trên màn hình chính
        View view = LayoutInflater.from(context).inflate(R.layout.list_flash_sale_main_screen, parent, false);
        return new FlashSaleViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull FlashSaleViewHolder holder, int position) {
        FlashSale flashSale = flashSaleList.get(position);
        Product product = productDAO.getProductById(flashSale.getProductId()); // Lấy thông tin Product từ ID

        if (product != null) {
            holder.productName.setText(product.getName());

            // Hiển thị giá gốc bị gạch ngang
            holder.productOriginalPrice.setVisibility(View.VISIBLE);
            String originalPriceFormatted = String.format(Locale.getDefault(), "%,.0f đ", product.getPrice());
            holder.productOriginalPrice.setText(originalPriceFormatted);
            holder.productOriginalPrice.setPaintFlags(holder.productOriginalPrice.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
            holder.productOriginalPrice.setTextColor(Color.GRAY);

            // Hiển thị giá đã giảm
            String salePriceFormatted = String.format(Locale.getDefault(), "%,.0f đ", flashSale.getSalePrice());
            holder.productPriceDiscount.setText(salePriceFormatted);
            holder.productPriceDiscount.setTextColor(Color.RED);
            holder.productPriceDiscount.setTypeface(null, Typeface.BOLD);
//            holder.productPriceDiscount.setTextSize(16sp); // Đảm bảo cỡ chữ to hơn (16sp) so với gốc (12sp)


            // TODO: Xử lý hiển thị ảnh sản phẩm
            // Nếu bạn có tên file ảnh trong product.getImage(), hãy sử dụng nó ở đây
            if (product.getImage() != null && !product.getImage().isEmpty()) {
                int imageResId = context.getResources().getIdentifier(product.getImage(), "drawable", context.getPackageName());
                if (imageResId != 0) {
                    holder.productImage.setImageResource(imageResId);
                } else {
                    holder.productImage.setImageResource(R.drawable.logo); // Fallback
                }
            } else {
                holder.productImage.setImageResource(R.drawable.logo); // Mặc định là logo
            }

        } else {
            // Xử lý trường hợp không tìm thấy sản phẩm
            holder.productName.setText("Sản phẩm không tồn tại");
            holder.productOriginalPrice.setVisibility(View.GONE);
            holder.productPriceDiscount.setText("");
            holder.productImage.setImageResource(R.drawable.logo);
        }

        holder.itemView.setOnClickListener(v -> {
            if (userId == -1) {
                Toast.makeText(context, "Vui lòng đăng nhập để xem chi tiết sản phẩm!", Toast.LENGTH_SHORT).show();
                return;
            }
            // Chức năng giống như trong ActivityFlashSale
            Intent intent = new Intent(context, ActivityProductDetail.class);
            intent.putExtra("productId", product.getId());
            intent.putExtra("userId", userId); // Truyền userId
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return flashSaleList.size();
    }

    public static class FlashSaleViewHolder extends RecyclerView.ViewHolder {
        ImageView productImage;
        TextView productName;
        TextView productOriginalPrice; // TextView cho giá gốc
        TextView productPriceDiscount; // TextView cho giá giảm

        public FlashSaleViewHolder(@NonNull View itemView) {
            super(itemView);
            productImage = itemView.findViewById(R.id.imageViewProductImage);
            productName = itemView.findViewById(R.id.textViewProductName);
            productOriginalPrice = itemView.findViewById(R.id.textViewProductPriceOriginal);
            productPriceDiscount = itemView.findViewById(R.id.textViewProductPriceDiscount);
        }
    }
}