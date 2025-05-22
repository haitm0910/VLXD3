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

public class ActivityBasket extends AppCompatActivity implements CartUpdateListener { // <-- THÊM implements CartUpdateListener
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_basket);

        basketListView = findViewById(R.id.basket_list_view);
        totalPriceTextView = findViewById(R.id.total_price);
        itemCountTextView = findViewById(R.id.item_count_text);
        emptyBasketMessage = findViewById(R.id.empty_basket_message);
        summaryCheckoutContainer = findViewById(R.id.summary_checkout_container);

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

        // Khởi tạo adapter với listener là chính Activity này
        adapter = new BasketAdapter(this, new ArrayList<>(), new ArrayList<>(), flashSaleDAO, this); // Khởi tạo với list rỗng ban đầu

        basketListView.setAdapter(adapter);

        // Gọi phương thức để tải và hiển thị dữ liệu giỏ hàng ban đầu
        refreshCartData();

        // Xử lý bottom navigation
        LinearLayout bottomNav = findViewById(R.id.bottomNavigationView);
        if (bottomNav != null && bottomNav.getChildCount() >= 2) {
            LinearLayout homeLayout = (LinearLayout) bottomNav.getChildAt(0);
            LinearLayout basketLayout = (LinearLayout) bottomNav.getChildAt(1);

            ImageView homeIcon = homeLayout.findViewById(R.id.home_icon_bottom_nav);
            if (homeIcon != null) homeIcon.setImageResource(R.drawable.home);

            ImageView basketIcon = basketLayout.findViewById(R.id.basket_icon_bottom_nav);
            if (basketIcon != null) basketIcon.setImageResource(R.drawable.basket);

            homeLayout.setOnClickListener(v -> {
                Intent intent = new Intent(ActivityBasket.this, MainActivity.class);
                intent.putExtra("userId", userId);
                startActivity(intent);
                finish();
            });
            basketLayout.setOnClickListener(v -> {
                // Đã ở ActivityBasket, có thể refresh hoặc không làm gì
            });
        }
    }

    // Phương thức để tải lại dữ liệu giỏ hàng và cập nhật UI
    private void refreshCartData() {
        cartItems = cartDAO.getCartItems(userId);
        products = new ArrayList<>(); // Tạo lại list products để đảm bảo đồng bộ
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

        // Cập nhật dữ liệu cho Adapter
        adapter = new BasketAdapter(this, cartItems, products, flashSaleDAO, this); // Tạo lại adapter hoặc cập nhật dữ liệu của adapter hiện có
        basketListView.setAdapter(adapter); // Set lại adapter
        // Hoặc nếu bạn muốn cập nhật dữ liệu mà không tạo lại adapter:
        // adapter.setCartItems(cartItems); // Nếu bạn thêm setter vào BasketAdapter
        // adapter.setProducts(products); // Nếu bạn thêm setter vào BasketAdapter
        // adapter.notifyDataSetChanged();

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
        // Khi giỏ hàng được cập nhật từ Adapter (thêm, xóa, sửa số lượng), gọi lại refreshCartData
        refreshCartData();
    }
}