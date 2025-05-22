// File: ActivityBasket.java

package com.example.vlxd3;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast; // Thêm import này
import android.widget.ImageView; // Thêm import này

import androidx.appcompat.app.AppCompatActivity;

import com.example.vlxd3.dao.CartDAO;
import com.example.vlxd3.dao.ProductDAO;
import com.example.vlxd3.model.CartItem;
import com.example.vlxd3.model.Product;

import java.util.ArrayList;
import java.util.List;

public class ActivityBasket extends AppCompatActivity {
    private ListView basketListView;
    private TextView totalPriceTextView;
    private CartDAO cartDAO;
    private ProductDAO productDAO;
    private List<CartItem> cartItems;
    private List<Product> products;
    private BasketAdapter adapter;
    private int userId; // <-- KHAI BÁO BIẾN userId
    private TextView itemCountTextView;
    private TextView emptyBasketMessage;
    private LinearLayout summaryCheckoutContainer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_basket);

        basketListView = findViewById(R.id.basket_list_view);
        totalPriceTextView = findViewById(R.id.total_price);
        itemCountTextView = findViewById(R.id.item_count_text);
        emptyBasketMessage = findViewById(R.id.empty_basket_message);
        summaryCheckoutContainer = findViewById(R.id.summary_checkout_container);

        // NHẬN userId TỪ INTENT
        userId = getIntent().getIntExtra("userId", -1); // <-- THÊM DÒNG NÀY

        // Kiểm tra userId, nếu là -1 (chưa đăng nhập), chuyển về LoginActivity
        if (userId == -1) {
            Toast.makeText(this, "Bạn cần đăng nhập để xem giỏ hàng!", Toast.LENGTH_SHORT).show();
            Intent loginIntent = new Intent(this, LoginActivity.class);
            startActivity(loginIntent);
            finish(); // Đóng ActivityBasket nếu chưa đăng nhập
            return; // Kết thúc onCreate
        }

        cartDAO = new CartDAO(this);
        productDAO = new ProductDAO(this);

        cartItems = cartDAO.getCartItems(userId); // Lấy giỏ hàng của userId hiện tại

        products = new ArrayList<>();
        double total = 0;
        for (CartItem item : cartItems) {
            Product p = productDAO.getProductById(item.getProductId());
            if (p != null) {
                products.add(p);
                total += p.getPrice() * item.getQuantity();
            }
        }

        itemCountTextView.setText(cartItems.size() + " sản phẩm");

        if (cartItems.isEmpty()) {
            emptyBasketMessage.setVisibility(View.VISIBLE);
            emptyBasketMessage.setText("Giỏ hàng của bạn đang trống!");
            basketListView.setVisibility(View.GONE);
            summaryCheckoutContainer.setVisibility(View.GONE);
        } else {
            emptyBasketMessage.setVisibility(View.GONE);
            basketListView.setVisibility(View.VISIBLE);
            summaryCheckoutContainer.setVisibility(View.VISIBLE);
        }

        adapter = new BasketAdapter(this, cartItems, products);
        basketListView.setAdapter(adapter);

        if (totalPriceTextView != null) {
            totalPriceTextView.setText(total + " đ");
        }

        // Xử lý bottom navigation
        LinearLayout bottomNav = findViewById(R.id.bottomNavigationView);
        if (bottomNav != null && bottomNav.getChildCount() >= 2) {
            LinearLayout homeLayout = (LinearLayout) bottomNav.getChildAt(0);
            LinearLayout basketLayout = (LinearLayout) bottomNav.getChildAt(1);

            // Gán icon cho home và basket trong bottom navigation của activity_basket.xml
            // Bạn cần đảm bảo các ImageView này có ID trong activity_basket.xml
            ImageView homeIcon = homeLayout.findViewById(R.id.home_icon_bottom_nav);
            if (homeIcon != null) homeIcon.setImageResource(R.drawable.home);

            ImageView basketIcon = basketLayout.findViewById(R.id.basket_icon_bottom_nav);
            if (basketIcon != null) basketIcon.setImageResource(R.drawable.basket);


            homeLayout.setOnClickListener(v -> {
                Intent intent = new Intent(ActivityBasket.this, MainActivity.class);
                intent.putExtra("userId", userId); // <-- TRUYỀN userId KHI QUAY VỀ MAIN
                startActivity(intent);
                finish();
            });
            basketLayout.setOnClickListener(v -> {
                // Đã ở ActivityBasket, có thể refresh hoặc không làm gì
            });
        }
    }
}