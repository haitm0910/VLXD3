// File: ActivityProductDetail.java

package com.example.vlxd3;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.vlxd3.dao.CartDAO;
import com.example.vlxd3.dao.FlashSaleDAO;
import com.example.vlxd3.dao.ProductDAO;
import com.example.vlxd3.model.CartItem;
import com.example.vlxd3.model.FlashSale;
import com.example.vlxd3.model.Product;

import java.util.Locale;

public class ActivityProductDetail extends AppCompatActivity {
    private TextView productName, productPrice, productAvailability, productStock, productDescription, quantityTextView;
    private TextView productOriginalPrice;
    private Button quantityIncreaseButton, quantityDecreaseButton, addToCartButton;
    private int quantity = 1;
    private Product product;
    private int userId;
    private ProductDAO productDAO;
    private FlashSaleDAO flashSaleDAO;
    private double currentProductPrice; // Biến để lưu giá hiện tại của sản phẩm (có thể là giá sale)

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_product_detail);

        ImageView backButton = findViewById(R.id.backButton);
        if (backButton != null) {
            backButton.setOnClickListener(v -> finish());
        }

        int productId = getIntent().getIntExtra("productId", -1);
        userId = getIntent().getIntExtra("userId", -1);

        productDAO = new ProductDAO(this);
        flashSaleDAO = new FlashSaleDAO(this);
        product = productDAO.getProductById(productId);

        productName = findViewById(R.id.product_name);
        productPrice = findViewById(R.id.product_price);
        productOriginalPrice = findViewById(R.id.product_original_price);
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

            // Kiểm tra xem sản phẩm có trong flash sale không
            FlashSale flashSale = flashSaleDAO.getFlashSaleByProductId(product.getId());

            if (flashSale != null) {
                // Có flash sale
                productOriginalPrice.setVisibility(View.VISIBLE); // Hiển thị TextView giá gốc
                String originalPriceFormatted = String.format(Locale.getDefault(), "%,.0f đ", product.getPrice());
                productOriginalPrice.setText(originalPriceFormatted);
                productOriginalPrice.setPaintFlags(productOriginalPrice.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG); // Gạch ngang giá gốc
                productOriginalPrice.setTextColor(Color.GRAY); // Đặt màu xám cho giá gốc

                String salePriceFormatted = String.format(Locale.getDefault(), "%,.0f đ", flashSale.getSalePrice());
                productPrice.setText(salePriceFormatted);
                productPrice.setTextColor(Color.RED); // Đặt màu đỏ cho giá sale
                productPrice.setTypeface(null, Typeface.BOLD); // Đặt in đậm cho giá sale
                currentProductPrice = flashSale.getSalePrice(); // <-- Gán giá sale
            } else {
                // Không có flash sale, chỉ hiển thị giá thường
                productOriginalPrice.setVisibility(View.GONE); // Ẩn TextView giá gốc
                String normalPriceFormatted = String.format(Locale.getDefault(), "%,.0f đ", product.getPrice());
                productPrice.setText(normalPriceFormatted);
                productPrice.setTextColor(Color.BLACK); // Đặt màu đen hoặc màu mặc định
                productPrice.setTypeface(null, Typeface.NORMAL); // Đặt bình thường
                currentProductPrice = product.getPrice(); // <-- Gán giá gốc
            }

            productStock.setText("Số lượng còn lại: " + product.getStock());
            productDescription.setText(product.getDescription());
            if (product.getStock() > 0) {
                productAvailability.setText("Tình trạng: Còn hàng");
            } else {
                productAvailability.setText("Tình trạng: Hết hàng");
            }
        }

        quantityTextView.setText(String.valueOf(quantity));

        quantityIncreaseButton.setOnClickListener(v -> {
            if (product != null && quantity < product.getStock()) {
                quantity++;
                quantityTextView.setText(String.valueOf(quantity));
            }
        });

        quantityDecreaseButton.setOnClickListener(v -> {
            if (quantity > 1) {
                quantity--;
                quantityTextView.setText(String.valueOf(quantity));
            }
        });

        addToCartButton.setOnClickListener(v -> {
            if (product != null && product.getStock() > 0) {
                if (userId == -1) {
                    Toast.makeText(this, "Bạn cần đăng nhập để thêm sản phẩm vào giỏ hàng!", Toast.LENGTH_SHORT).show();
                    Intent loginIntent = new Intent(this, LoginActivity.class);
                    startActivity(loginIntent);
                    return;
                }
                CartDAO cartDAO = new CartDAO(this);
                // CartItem chỉ lưu productId và quantity. Giá được tính toán khi hiển thị giỏ hàng
                CartItem cartItem = new CartItem(userId, product.getId(), quantity);
                long result = cartDAO.addToCart(cartItem);
                if (result != -1) {
                    Toast.makeText(this, "Đã thêm sản phẩm vào giỏ hàng thành công!", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(this, "Thêm sản phẩm vào giỏ hàng thất bại!", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(this, "Sản phẩm đã hết hàng!", Toast.LENGTH_SHORT).show();
            }
        });
    }
}