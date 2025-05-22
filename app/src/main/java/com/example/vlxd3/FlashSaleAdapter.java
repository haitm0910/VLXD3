// File: FlashSaleAdapter.java

package com.example.vlxd3;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView; // Thêm import này
import android.widget.TextView;

import com.example.vlxd3.dao.ProductDAO; // Thêm import này
import com.example.vlxd3.model.FlashSale; // Thêm import này
import com.example.vlxd3.model.Product;

import java.util.List;
import java.util.Locale; // Thêm import này

public class FlashSaleAdapter extends BaseAdapter {
    private Context context;
    private List<FlashSale> flashSales; // <-- Đã thay đổi kiểu dữ liệu ở đây
    private ProductDAO productDAO; // Khai báo ProductDAO

    // Sửa constructor để nhận List<FlashSale>
    public FlashSaleAdapter(Context context, List<FlashSale> flashSales) { // <-- Đã sửa ở đây
        this.context = context;
        this.flashSales = flashSales;
        this.productDAO = new ProductDAO(context); // Khởi tạo ProductDAO
    }

    @Override
    public int getCount() {
        return flashSales.size();
    }

    @Override
    public Object getItem(int position) {
        return flashSales.get(position);
    }

    @Override
    public long getItemId(int position) {
        return flashSales.get(position).getId();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.list_single_flash_sale, parent, false);
        }

        TextView nameTextView = convertView.findViewById(R.id.product_name);
        TextView originalPriceTextView = convertView.findViewById(R.id.product_original_price); // Ánh xạ TextView giá gốc
        TextView salePriceTextView = convertView.findViewById(R.id.product_price); // Ánh xạ TextView giá sale (đã đổi tên biến để rõ ràng hơn)
        ImageView productImageView = convertView.findViewById(R.id.product_image); // Ánh xạ ImageView

        FlashSale flashSale = flashSales.get(position);
        Product product = productDAO.getProductById(flashSale.getProductId()); // Lấy thông tin Product từ ID

        if (product != null) {
            nameTextView.setText(product.getName());

            // Hiển thị giá gốc bị gạch ngang
            originalPriceTextView.setVisibility(View.VISIBLE);
            String originalPriceFormatted = String.format(Locale.getDefault(), "%,.0f đ", product.getPrice());
            originalPriceTextView.setText(originalPriceFormatted);
            originalPriceTextView.setPaintFlags(originalPriceTextView.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
            originalPriceTextView.setTextColor(Color.GRAY);

            // Hiển thị giá sale
            String salePriceFormatted = String.format(Locale.getDefault(), "%,.0f đ", flashSale.getSalePrice());
            salePriceTextView.setText(salePriceFormatted);
            salePriceTextView.setTextColor(Color.RED);
            salePriceTextView.setTypeface(null, Typeface.BOLD);

            // Tùy chọn: Thêm logic tải ảnh ở đây
            // productImageView.setImageResource(...)
        } else {
            // Xử lý trường hợp không tìm thấy sản phẩm
            nameTextView.setText("Sản phẩm không tồn tại");
            originalPriceTextView.setVisibility(View.GONE);
            salePriceTextView.setText("");
            productImageView.setImageResource(R.drawable.logo); // Đặt ảnh mặc định
        }

        return convertView;
    }
}