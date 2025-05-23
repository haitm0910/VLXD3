package com.example.vlxd3;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.os.Bundle;
import android.util.Log; // Import Log for debugging
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout; // For image indicators
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager.widget.ViewPager; // Import ViewPager

import com.example.vlxd3.adapter.ProductImagePagerAdapter; // Import your new adapter
import com.example.vlxd3.dao.CartDAO;
import com.example.vlxd3.dao.FlashSaleDAO;
import com.example.vlxd3.dao.ProductDAO;
import com.example.vlxd3.model.CartItem;
import com.example.vlxd3.model.FlashSale;
import com.example.vlxd3.model.Product;

import java.util.ArrayList; // For list of image names
import java.util.List; // For list of image names
import java.util.Locale;

public class ActivityProductDetail extends AppCompatActivity {
    private TextView productName, productPrice, productAvailability, productStock, productDescription, quantityTextView;
    private TextView productOriginalPrice;
    private Button quantityIncreaseButton, quantityDecreaseButton, addToCartButton;
    private ViewPager productImagePager; // Declare ViewPager
    private LinearLayout imageIndicatorLayout; // Declare layout for indicators

    private int quantity = 1;
    private Product product;
    private int userId;
    private ProductDAO productDAO;
    private FlashSaleDAO flashSaleDAO;

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
        flashSaleDAO = new FlashSaleDAO(this);
        product = productDAO.getProductById(productId);

        // Ánh xạ Views
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
        productImagePager = findViewById(R.id.product_image_pager); // Ánh xạ ViewPager
        imageIndicatorLayout = findViewById(R.id.image_indicator_layout); // Ánh xạ indicator layout

        // Hiển thị thông tin sản phẩm
        if (product != null) {
            productName.setText(product.getName());

            // --- Set Product Images using ViewPager ---
            List<String> imageNames = new ArrayList<>();
            // Currently, your product model only holds one image name.
            // If you want multiple images, you'd need to modify your database schema
            // and how you fetch product images (e.g., a separate table for product images).
            // For now, we put the single image name into a list.
            if (product.getImage() != null && !product.getImage().isEmpty()) {
                imageNames.add(product.getImage());
            }
            // Add a placeholder image if no specific image is available
            if (imageNames.isEmpty()) {
                imageNames.add("placeholder_image"); // Make sure you have placeholder_image.png in drawable
            }

            ProductImagePagerAdapter pagerAdapter = new ProductImagePagerAdapter(this, imageNames);
            productImagePager.setAdapter(pagerAdapter);

            // Setup indicators (optional but good UI)
            setupImageIndicators(imageNames.size());
            productImagePager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
                @Override
                public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                    // Not used for simple indicator
                }

                @Override
                public void onPageSelected(int position) {
                    updateImageIndicators(position);
                }

                @Override
                public void onPageScrollStateChanged(int state) {
                    // Not used for simple indicator
                }
            });
            // --- End Set Product Images ---

            // --- LOGIC HIỂN THỊ GIÁ (SALE vs GỐC) ---
            FlashSale flashSale = flashSaleDAO.getFlashSaleByProductId(product.getId());

            if (flashSale != null) {
                // SẢN PHẨM ĐANG TRONG FLASH SALE
                productOriginalPrice.setVisibility(View.VISIBLE);
                String originalPriceFormatted = String.format(Locale.getDefault(), "%,.0f đ", product.getPrice());
                productOriginalPrice.setText(originalPriceFormatted);
                productOriginalPrice.setPaintFlags(productOriginalPrice.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
                productOriginalPrice.setTextColor(Color.GRAY);

                String salePriceFormatted = String.format(Locale.getDefault(), "%,.0f đ", flashSale.getSalePrice());
                productPrice.setText(salePriceFormatted);
                productPrice.setTextColor(Color.RED);
                productPrice.setTypeface(null, Typeface.BOLD);
            } else {
                // SẢN PHẨM KHÔNG TRONG FLASH SALE (hoặc flash sale đã hết hạn)
                productOriginalPrice.setVisibility(View.GONE);
                String normalPriceFormatted = String.format(Locale.getDefault(), "%,.0f đ", product.getPrice());
                productPrice.setText(normalPriceFormatted);
                productPrice.setTextColor(Color.BLACK);
                productPrice.setTypeface(null, Typeface.NORMAL);
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
            finish();
            return;
        }

        quantityTextView.setText(String.valueOf(quantity));

        // Xử lý nút tăng số lượng
        quantityIncreaseButton.setOnClickListener(v -> {
            if (product != null && quantity < product.getStock()) {
                quantity++;
                quantityTextView.setText(String.valueOf(quantity));
            } else if (product != null) {
                Toast.makeText(this, "Số lượng đã đạt tối đa trong kho!", Toast.LENGTH_SHORT).show();
            }
        });

        // Xử lý nút giảm số lượng
        quantityDecreaseButton.setOnClickListener(v -> {
            if (quantity > 1) {
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

    // Helper methods for image indicators
    private ImageView[] dots;
    private void setupImageIndicators(int count) {
        dots = new ImageView[count];
        imageIndicatorLayout.removeAllViews(); // Clear any existing dots

        for (int i = 0; i < count; i++) {
            dots[i] = new ImageView(this);
            dots[i].setImageDrawable(getResources().getDrawable(R.drawable.dot_inactive)); // Make sure you have dot_inactive.xml in drawable
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
            );
            params.setMargins(8, 0, 8, 0);
            imageIndicatorLayout.addView(dots[i], params);
        }
        if (dots.length > 0) {
            dots[0].setImageDrawable(getResources().getDrawable(R.drawable.dot_active)); // Make sure you have dot_active.xml in drawable
        }
    }

    private void updateImageIndicators(int position) {
        for (int i = 0; i < dots.length; i++) {
            dots[i].setImageDrawable(getResources().getDrawable(R.drawable.dot_inactive));
        }
        if (dots.length > 0) {
            dots[position].setImageDrawable(getResources().getDrawable(R.drawable.dot_active));
        }
    }
}