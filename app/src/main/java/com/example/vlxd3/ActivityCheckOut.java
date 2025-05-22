// File: ActivityCheckOut.java
package com.example.vlxd3;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager; // Import này
import androidx.recyclerview.widget.RecyclerView; // Import này

import com.example.vlxd3.dao.CartDAO;
import com.example.vlxd3.dao.FlashSaleDAO;
import com.example.vlxd3.dao.OrderDAO;
import com.example.vlxd3.dao.ProductDAO;
import com.example.vlxd3.dao.UserDAO; // Import UserDAO
import com.example.vlxd3.model.CartItem;
import com.example.vlxd3.model.FlashSale;
import com.example.vlxd3.model.Order;
import com.example.vlxd3.model.Product;
import com.example.vlxd3.model.User; // Import User

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class ActivityCheckOut extends AppCompatActivity {
    private EditText nameEditText, addressEditText, phoneEditText;
    private RadioGroup paymentMethodRadioGroup;
    private RadioButton codRadioButton, bankTransferRadioButton;
    private TextView totalAmountTextView;
    private Button confirmOrderButton;
    private RecyclerView orderSummaryRecyclerView;

    private int userId;
    private UserDAO userDAO;
    private CartDAO cartDAO;
    private ProductDAO productDAO;
    private FlashSaleDAO flashSaleDAO;
    private OrderDAO orderDAO;

    private List<CartItem> cartItems;
    private List<Product> productsInCart; // Sản phẩm tương ứng với cartItems
    private double currentTotalPrice = 0; // Tổng tiền trước VAT

    private static final double VAT_PERCENTAGE = 0.05; // 5% VAT

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_check_out);

        // Ánh xạ các View
        nameEditText = findViewById(R.id.name_edit_text);
        addressEditText = findViewById(R.id.address_edit_text);
        phoneEditText = findViewById(R.id.phone_edit_text);
        paymentMethodRadioGroup = findViewById(R.id.payment_method_radio_group);
        codRadioButton = findViewById(R.id.cod_radio_button);
        bankTransferRadioButton = findViewById(R.id.bank_transfer_radio_button);
        totalAmountTextView = findViewById(R.id.total_amount_text_view);
        confirmOrderButton = findViewById(R.id.confirm_order_button);
        orderSummaryRecyclerView = findViewById(R.id.order_summary_recycler_view);

        // Khởi tạo DAOs
        userDAO = new UserDAO(this);
        cartDAO = new CartDAO(this);
        productDAO = new ProductDAO(this);
        flashSaleDAO = new FlashSaleDAO(this);
        orderDAO = new OrderDAO(this);

        // Lấy userId từ Intent
        userId = getIntent().getIntExtra("userId", -1);
        if (userId == -1) {
            Toast.makeText(this, "Không tìm thấy thông tin người dùng. Vui lòng đăng nhập lại.", Toast.LENGTH_LONG).show();
            finish(); // Đóng ActivityCheckout
            return;
        }

        // Tải thông tin người dùng và giỏ hàng
        loadUserInfo();
        loadCartItemsAndCalculateTotal();
        setupOrderSummaryRecyclerView();

        // Xử lý nút Xác nhận đơn hàng
        confirmOrderButton.setOnClickListener(v -> confirmOrder());
    }

    private void loadUserInfo() {
        User user = userDAO.getUserById(userId);
        if (user != null) {
            // Điền sẵn thông tin từ database (nếu có)
            nameEditText.setText(user.getFullName());
            phoneEditText.setText(user.getPhone());
            // Địa chỉ cần được lưu trong User model nếu muốn tự động điền
            // Hiện tại User model của bạn chưa có trường address
            // Nếu bạn muốn lưu địa chỉ, bạn cần thêm trường 'address' vào User model và UserDAO
        }
    }

    private void loadCartItemsAndCalculateTotal() {
        cartItems = cartDAO.getCartItems(userId);
        productsInCart = new ArrayList<>();
        currentTotalPrice = 0; // Reset tổng tiền

        if (cartItems.isEmpty()) {
            Toast.makeText(this, "Giỏ hàng trống. Không thể thanh toán.", Toast.LENGTH_LONG).show();
            finish(); // Đóng ActivityCheckout nếu giỏ hàng trống
            return;
        }

        for (CartItem item : cartItems) {
            Product p = productDAO.getProductById(item.getProductId());
            if (p != null) {
                productsInCart.add(p);
                FlashSale flashSale = flashSaleDAO.getFlashSaleByProductId(p.getId());
                if (flashSale != null) {
                    currentTotalPrice += flashSale.getSalePrice() * item.getQuantity();
                } else {
                    currentTotalPrice += p.getPrice() * item.getQuantity();
                }
            }
        }

        // Tính tổng tiền bao gồm VAT
        double finalTotalAmount = currentTotalPrice * (1 + VAT_PERCENTAGE);
        totalAmountTextView.setText(String.format(Locale.getDefault(), "Tổng tiền: %,.0f đ", finalTotalAmount));
    }

    private void setupOrderSummaryRecyclerView() {
        // Bạn sẽ cần một Adapter riêng cho RecyclerView này để hiển thị tóm tắt đơn hàng
        // Ví dụ: OrderSummaryAdapter (sẽ không cung cấp code adapter này ở đây để tập trung vào chức năng chính)
        // Hiện tại, bạn có thể chỉ cần hiển thị tổng tiền chung.
        // Nếu muốn hiển thị danh sách sản phẩm trong RecyclerView, bạn sẽ cần tạo OrderSummaryAdapter
        // và OrderSummaryItem (nếu cần một model riêng cho item trong tóm tắt)
        orderSummaryRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        // Ví dụ: orderSummaryRecyclerView.setAdapter(new OrderSummaryAdapter(this, cartItems, productsInCart, flashSaleDAO));
        // Nếu không có adapter cho RecyclerView, bạn có thể xóa RecyclerView khỏi layout hoặc chỉ hiển thị tổng tiền.

        // Tạm thời, để hiển thị list sản phẩm, chúng ta sẽ tạo một adapter đơn giản ngay đây
        // Trong một dự án lớn hơn, nên tạo file riêng cho adapter này
        orderSummaryRecyclerView.setAdapter(new OrderSummaryAdapter(this, cartItems, productsInCart, flashSaleDAO));
    }

    private void confirmOrder() {
        String customerName = nameEditText.getText().toString().trim();
        String customerAddress = addressEditText.getText().toString().trim();
        String customerPhone = phoneEditText.getText().toString().trim();

        if (customerName.isEmpty() || customerAddress.isEmpty() || customerPhone.isEmpty()) {
            Toast.makeText(this, "Vui lòng nhập đầy đủ thông tin giao hàng!", Toast.LENGTH_SHORT).show();
            return;
        }

        if (cartItems.isEmpty()) {
            Toast.makeText(this, "Giỏ hàng trống. Không thể đặt hàng.", Toast.LENGTH_SHORT).show();
            return;
        }

        String paymentMethod;
        int selectedRadioButtonId = paymentMethodRadioGroup.getCheckedRadioButtonId();
        if (selectedRadioButtonId == R.id.cod_radio_button) {
            paymentMethod = "Thanh toán khi nhận hàng (COD)";
        } else if (selectedRadioButtonId == R.id.bank_transfer_radio_button) {
            paymentMethod = "Chuyển khoản ngân hàng";
        } else {
            Toast.makeText(this, "Vui lòng chọn phương thức thanh toán!", Toast.LENGTH_SHORT).show();
            return;
        }

        // Lấy ngày đặt hàng hiện tại
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        String orderDate = sdf.format(new Date());

        // Tính tổng tiền cuối cùng (bao gồm VAT)
        double finalTotalAmount = currentTotalPrice * (1 + VAT_PERCENTAGE);

        // Tạo đối tượng Order
        Order newOrder = new Order(
                userId,
                orderDate,
                finalTotalAmount,
                customerName,
                customerAddress,
                customerPhone,
                paymentMethod,
                "Đang chờ xác nhận" // Trạng thái ban đầu của đơn hàng
        );

        // Lưu đơn hàng vào database
        long orderId = orderDAO.createOrder(newOrder, cartItems);

        if (orderId != -1) {
            Toast.makeText(this, "Đặt hàng thành công! Mã đơn hàng của bạn: " + orderId, Toast.LENGTH_LONG).show();
            cartDAO.clearCart(userId); // Xóa giỏ hàng sau khi đặt hàng thành công
            // Chuyển về màn hình chính hoặc màn hình xác nhận đơn hàng
            Intent intent = new Intent(ActivityCheckOut.this, MainActivity.class);
            intent.putExtra("userId", userId); // Truyền userId về MainActivity
            startActivity(intent);
            finish(); // Đóng ActivityCheckOut
        } else {
            Toast.makeText(this, "Đặt hàng thất bại. Vui lòng thử lại!", Toast.LENGTH_LONG).show();
        }
    }

    // Adapter nội bộ đơn giản cho RecyclerView hiển thị tóm tắt đơn hàng
    // Trong thực tế, bạn nên tạo file riêng cho Adapter này
    private class OrderSummaryAdapter extends RecyclerView.Adapter<OrderSummaryAdapter.ViewHolder> {
        private Context adapterContext;
        private List<CartItem> adapterCartItems;
        private List<Product> adapterProducts;
        private FlashSaleDAO adapterFlashSaleDAO;

        public OrderSummaryAdapter(Context context, List<CartItem> cartItems, List<Product> products, FlashSaleDAO flashSaleDAO) {
            this.adapterContext = context;
            this.adapterCartItems = cartItems;
            this.adapterProducts = products;
            this.adapterFlashSaleDAO = flashSaleDAO;
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(adapterContext).inflate(R.layout.item_order_summary_product, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(ViewHolder holder, int position) {
            CartItem item = adapterCartItems.get(position);
            Product product = productDAO.getProductById(item.getProductId()); // Lấy product từ DAO

            if (product != null) {
                holder.productName.setText(product.getName());
                holder.productQuantity.setText("x" + item.getQuantity());

                double itemPrice;
                FlashSale flashSale = adapterFlashSaleDAO.getFlashSaleByProductId(product.getId());
                if (flashSale != null) {
                    itemPrice = flashSale.getSalePrice();
                } else {
                    itemPrice = product.getPrice();
                }
                holder.productPrice.setText(String.format(Locale.getDefault(), "%,.0f đ", itemPrice * item.getQuantity()));
            } else {
                holder.productName.setText("Sản phẩm không tồn tại");
                holder.productQuantity.setText("");
                holder.productPrice.setText("");
            }
        }

        @Override
        public int getItemCount() {
            return adapterCartItems.size();
        }

        public class ViewHolder extends RecyclerView.ViewHolder {
            TextView productName, productQuantity, productPrice;

            public ViewHolder(View itemView) {
                super(itemView);
                productName = itemView.findViewById(R.id.order_summary_product_name);
                productQuantity = itemView.findViewById(R.id.order_summary_product_quantity);
                productPrice = itemView.findViewById(R.id.order_summary_product_price);
            }
        }
    }
}