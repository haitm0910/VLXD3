// File: ActivityProductDetail.java

package com.example.vlxd3;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.vlxd3.dao.CartDAO;
import com.example.vlxd3.dao.ProductDAO;
import com.example.vlxd3.model.CartItem;
import com.example.vlxd3.model.Product;

public class ActivityProductDetail extends AppCompatActivity {
    private TextView productName, productPrice, productAvailability, productStock, productDescription, quantityTextView;
    private Button quantityIncreaseButton, quantityDecreaseButton, addToCartButton;
    private int quantity = 1;
    private Product product;
    private int userId; // <-- KHAI BÁO BIẾN userId

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_product_detail);

        // Thêm nút back (đã có trong file sửa lỗi trước)
        ImageView backButton = findViewById(R.id.backButton);
        if (backButton != null) {
            backButton.setOnClickListener(v -> finish());
        }

        int productId = getIntent().getIntExtra("productId", -1);
        // NHẬN userId TỪ INTENT
        userId = getIntent().getIntExtra("userId", -1); // <-- THÊM DÒNG NÀY

        ProductDAO productDAO = new ProductDAO(this);
        product = productDAO.getProductById(productId);

        productName = findViewById(R.id.product_name);
        productPrice = findViewById(R.id.product_price);
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
            productPrice.setText(product.getPrice() + " đ");
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
                if (userId == -1) { // Kiểm tra lại userId một lần nữa
                    Toast.makeText(this, "Bạn cần đăng nhập để thêm sản phẩm vào giỏ hàng!", Toast.LENGTH_SHORT).show();
                    Intent loginIntent = new Intent(this, LoginActivity.class);
                    startActivity(loginIntent);
                    return;
                }
                CartDAO cartDAO = new CartDAO(this);
                CartItem cartItem = new CartItem(userId, product.getId(), quantity); // SỬ DỤNG userId ĐÃ NHẬN
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

        // Kiểm tra nếu là sản phẩm Flash Sale
        boolean isFlashSale = getIntent().getBooleanExtra("isFlashSale", false);
        if (isFlashSale) {
            TextView discountedPrice = findViewById(R.id.product_price);
            if (discountedPrice != null) {
                discountedPrice.setVisibility(View.VISIBLE);
                discountedPrice.setText("Giá giảm: " + product.getDiscountedPrice() + " đ");
            }
        }
    }
}