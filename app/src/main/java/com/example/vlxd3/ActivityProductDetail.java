// File: ActivityProductDetail.java

package com.example.vlxd3;

import android.content.Intent;
import android.graphics.Color; // Import này để dùng màu sắc
import android.graphics.Paint; // Import này để dùng cờ gạch ngang
import android.graphics.Typeface; // Import này để dùng kiểu chữ đậm
import android.os.Bundle;
import android.view.View; // Import này để dùng View.VISIBLE/GONE
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.vlxd3.dao.CartDAO;
import com.example.vlxd3.dao.FlashSaleDAO; // Import này
import com.example.vlxd3.dao.ProductDAO;
import com.example.vlxd3.model.CartItem;
import com.example.vlxd3.model.FlashSale; // Import này
import com.example.vlxd3.model.Product;

import java.util.Locale; // Import này để định dạng số

public class ActivityProductDetail extends AppCompatActivity {
    private TextView productName, productPrice, productAvailability, productStock, productDescription, quantityTextView;
    private TextView productOriginalPrice; // TextView cho giá gốc (bị gạch ngang)
    private Button quantityIncreaseButton, quantityDecreaseButton, addToCartButton;
    private int quantity = 1;
    private Product product;
    private int userId;
    private ProductDAO productDAO;
    private FlashSaleDAO flashSaleDAO; // Khai báo FlashSaleDAO

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_product_detail);

        // Xử lý nút back
        ImageView backButton = findViewById(R.id.backButton);
        if (backButton != null) {
            backButton.setOnClickListener(v -> finish());
        }

        // Lấy dữ liệu từ Intent
        int productId = getIntent().getIntExtra("productId", -1);
        userId = getIntent().getIntExtra("userId", -1);

        // Khởi tạo DAOs
        productDAO = new ProductDAO(this);
        flashSaleDAO = new FlashSaleDAO(this); // Khởi tạo FlashSaleDAO
        product = productDAO.getProductById(productId); // Lấy thông tin sản phẩm

        // Ánh xạ Views
        productName = findViewById(R.id.product_name);
        productPrice = findViewById(R.id.product_price); // TextView cho giá hiện tại (giá sale hoặc giá gốc)
        productOriginalPrice = findViewById(R.id.product_original_price); // TextView cho giá gốc bị gạch ngang
        productAvailability = findViewById(R.id.product_availability);
        productStock = findViewById(R.id.product_stock);
        productDescription = findViewById(R.id.textView2);
        quantityTextView = findViewById(R.id.quantity_text_view);
        quantityIncreaseButton = findViewById(R.id.quantity_increase_button);
        quantityDecreaseButton = findViewById(R.id.quantity_decrease_button);
        addToCartButton = findViewById(R.id.add_to_cart_button);

        // Hiển thị thông tin sản phẩm
        if (product != null) {
            productName.setText(product.getName());

            // --- LOGIC HIỂN THỊ GIÁ (SALE vs GỐC) ---
            FlashSale flashSale = flashSaleDAO.getFlashSaleByProductId(product.getId()); // Kiểm tra flash sale

            if (flashSale != null) {
                // SẢN PHẨM ĐANG TRONG FLASH SALE
                productOriginalPrice.setVisibility(View.VISIBLE); // Hiển thị TextView giá gốc bị gạch ngang
                String originalPriceFormatted = String.format(Locale.getDefault(), "%,.0f đ", product.getPrice());
                productOriginalPrice.setText(originalPriceFormatted);
                productOriginalPrice.setPaintFlags(productOriginalPrice.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG); // Áp dụng gạch ngang
                productOriginalPrice.setTextColor(Color.GRAY); // Đặt màu xám cho giá gốc

                String salePriceFormatted = String.format(Locale.getDefault(), "%,.0f đ", flashSale.getSalePrice());
                productPrice.setText(salePriceFormatted); // Hiển thị giá sale
                productPrice.setTextColor(Color.RED); // Đặt màu đỏ cho giá sale
                productPrice.setTypeface(null, Typeface.BOLD); // Đặt in đậm cho giá sale
            } else {
                // SẢN PHẨM KHÔNG TRONG FLASH SALE (hoặc flash sale đã hết hạn)
                productOriginalPrice.setVisibility(View.GONE); // Ẩn TextView giá gốc bị gạch ngang
                String normalPriceFormatted = String.format(Locale.getDefault(), "%,.0f đ", product.getPrice());
                productPrice.setText(normalPriceFormatted); // Hiển thị giá gốc bình thường
                productPrice.setTextColor(Color.BLACK); // Đặt màu đen (hoặc màu mặc định)
                productPrice.setTypeface(null, Typeface.NORMAL); // Đặt kiểu chữ bình thường
            }
            // --- KẾT THÚC LOGIC HIỂN THỊ GIÁ ---


            productStock.setText("Số lượng còn lại: " + product.getStock());
            productDescription.setText(product.getDescription());
            if (product.getStock() > 0) {
                productAvailability.setText("Tình trạng: Còn hàng");
            } else {
                productAvailability.setText("Tình trạng: Hết hàng");
            }
        } else {
            // Xử lý trường hợp không tìm thấy sản phẩm
            Toast.makeText(this, "Không tìm thấy thông tin sản phẩm.", Toast.LENGTH_SHORT).show();
            finish(); // Đóng Activity nếu không tìm thấy sản phẩm
            return;
        }

        quantityTextView.setText(String.valueOf(quantity));

        // Xử lý nút tăng số lượng
        quantityIncreaseButton.setOnClickListener(v -> {
            if (product != null && quantity < product.getStock()) { // Đảm bảo không vượt quá tồn kho
                quantity++;
                quantityTextView.setText(String.valueOf(quantity));
            } else if (product != null) {
                Toast.makeText(this, "Số lượng đã đạt tối đa trong kho!", Toast.LENGTH_SHORT).show();
            }
        });

        // Xử lý nút giảm số lượng
        quantityDecreaseButton.setOnClickListener(v -> {
            if (quantity > 1) { // Giảm số lượng tối thiểu là 1
                quantity--;
                quantityTextView.setText(String.valueOf(quantity));
            }
        });

        // Xử lý nút "Thêm vào giỏ hàng"
        addToCartButton.setOnClickListener(v -> {
            if (product != null && product.getStock() > 0) {
                if (userId == -1) {
                    Toast.makeText(this, "Bạn cần đăng nhập để thêm sản phẩm vào giỏ hàng!", Toast.LENGTH_SHORT).show();
                    Intent loginIntent = new Intent(this, LoginActivity.class);
                    startActivity(loginIntent);
                    return;
                }
                CartDAO cartDAO = new CartDAO(this);
                CartItem cartItem = new CartItem(userId, product.getId(), quantity);
                long result = cartDAO.addToCart(cartItem);
                if (result != -1) {
                    Toast.makeText(this, "Đã thêm sản phẩm vào giỏ hàng thành công!", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(this, "Thêm sản phẩm vào giỏ hàng thất bại!", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(this, "Sản phẩm đã hết hàng hoặc không có sẵn!", Toast.LENGTH_SHORT).show();
            }
        });
    }
}