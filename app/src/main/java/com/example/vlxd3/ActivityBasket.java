// File: ActivityBasket.java

package com.example.vlxd3;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ImageView;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

import com.example.vlxd3.dao.CartDAO;
import com.example.vlxd3.dao.FlashSaleDAO;
import com.example.vlxd3.dao.ProductDAO;
import com.example.vlxd3.model.CartItem;
import com.example.vlxd3.model.FlashSale;
import com.example.vlxd3.model.Product;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class ActivityBasket extends AppCompatActivity implements CartUpdateListener {
    private ListView basketListView;
    private TextView totalPriceTextView;
    private CartDAO cartDAO;
    private ProductDAO productDAO;
    private FlashSaleDAO flashSaleDAO;
    private List<CartItem> cartItems;
    private List<Product> products;
    private BasketAdapter adapter;
    private int userId;
    private TextView itemCountTextView;
    private TextView emptyBasketMessage;
    private LinearLayout summaryCheckoutContainer;
    private Button goToCheckoutButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_basket);

        basketListView = findViewById(R.id.basket_list_view);
        totalPriceTextView = findViewById(R.id.total_price);
        itemCountTextView = findViewById(R.id.item_count_text);
        emptyBasketMessage = findViewById(R.id.empty_basket_message);
        summaryCheckoutContainer = findViewById(R.id.summary_checkout_container);
        goToCheckoutButton = findViewById(R.id.go_to_checkout_button);

        userId = getIntent().getIntExtra("userId", -1);
        if (userId == -1) {
            Toast.makeText(this, "Bạn cần đăng nhập để xem giỏ hàng!", Toast.LENGTH_SHORT).show();
            Intent loginIntent = new Intent(this, LoginActivity.class);
            startActivity(loginIntent);
            finish();
            return;
        }

        cartDAO = new CartDAO(this);
        productDAO = new ProductDAO(this);
        flashSaleDAO = new FlashSaleDAO(this);

        adapter = new BasketAdapter(this, new ArrayList<>(), new ArrayList<>(), flashSaleDAO, this);
        basketListView.setAdapter(adapter);

        refreshCartData();

        goToCheckoutButton.setOnClickListener(v -> {
            if (cartItems.isEmpty()) {
                Toast.makeText(this, "Giỏ hàng trống. Không thể thanh toán.", Toast.LENGTH_SHORT).show();
                return;
            }
            Intent intent = new Intent(ActivityBasket.this, ActivityCheckOut.class);
            intent.putExtra("userId", userId);
            startActivity(intent);
        });

        LinearLayout bottomNav = findViewById(R.id.bottomNavigationView);
        if (bottomNav != null && bottomNav.getChildCount() >= 2) {
            LinearLayout homeLayout = (LinearLayout) bottomNav.getChildAt(0);
            LinearLayout basketLayout = (LinearLayout) bottomNav.getChildAt(1);
            LinearLayout accountLayout = (LinearLayout) bottomNav.getChildAt(2);

            ImageView homeIcon = homeLayout.findViewById(R.id.home_icon_bottom_nav);
            if(homeIcon != null) homeIcon.setImageResource(R.drawable.home);
            ImageView basketIcon = basketLayout.findViewById(R.id.basket_icon_bottom_nav);
            if(basketIcon != null) basketIcon.setImageResource(R.drawable.basket);
            ImageView accountIcon = accountLayout.findViewById(R.id.account_icon_bottom_nav);
            if(accountIcon != null) accountIcon.setImageResource(R.drawable.account);

            homeLayout.setOnClickListener(v -> {
                Intent intent = new Intent(ActivityBasket.this, MainActivity.class);
                intent.putExtra("userId", userId);
                startActivity(intent);
                finish();
            });
            basketLayout.setOnClickListener(v -> {
                // Đã ở ActivityBasket, có thể refresh hoặc không làm gì
            });
            // SỬA SỰ KIỆN CLICK CHO NÚT "TÀI KHOẢN"
            accountLayout.setOnClickListener(v -> {
                Intent intent = new Intent(ActivityBasket.this, AccountActivity.class); // <-- Đã sửa ở đây
                intent.putExtra("userId", userId);
                startActivity(intent);
                finish(); // Đóng Activity hiện tại nếu muốn
            });
        }
    }

    private void refreshCartData() {
        cartItems = cartDAO.getCartItems(userId);
        products = new ArrayList<>();
        double total = 0;

        for (CartItem item : cartItems) {
            Product p = productDAO.getProductById(item.getProductId());
            if (p != null) {
                products.add(p);
                FlashSale flashSale = flashSaleDAO.getFlashSaleByProductId(p.getId());
                if (flashSale != null) {
                    total += flashSale.getSalePrice() * item.getQuantity();
                } else {
                    total += p.getPrice() * item.getQuantity();
                }
            }
        }

        adapter = new BasketAdapter(this, cartItems, products, flashSaleDAO, this);
        basketListView.setAdapter(adapter);

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

        if (totalPriceTextView != null) {
            totalPriceTextView.setText(String.format(Locale.getDefault(), "%,.0f đ", total));
        }
    }

    @Override
    public void onCartUpdated() {
        refreshCartData();
    }
}