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
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.vlxd3.dao.CartDAO;
import com.example.vlxd3.dao.FlashSaleDAO;
import com.example.vlxd3.dao.OrderDAO;
import com.example.vlxd3.dao.ProductDAO;
import com.example.vlxd3.dao.UserDAO;
import com.example.vlxd3.model.CartItem;
import com.example.vlxd3.model.FlashSale;
import com.example.vlxd3.model.Order;
import com.example.vlxd3.model.Product;
import com.example.vlxd3.model.User;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class ActivityCheckOut extends AppCompatActivity {
    private EditText nameEditText, addressEditText, phoneEditText;
    private RadioGroup paymentMethodRadioGroup;
    private RadioButton codRadioButton, bankTransferRadioButton;

    // Các TextView mới cho tổng tiền
    private TextView subtotalAmountTextView; // TextView cho tổng tiền hàng (trước VAT)
    private TextView vatAmountTextView;      // TextView cho phí VAT
    private TextView finalTotalAmountTextView; // TextView cho tổng tiền cuối cùng (bao gồm VAT) - đây là total_amount_text_view cũ

    private Button confirmOrderButton;
    private RecyclerView orderSummaryRecyclerView;

    private int userId;
    private UserDAO userDAO;
    private CartDAO cartDAO;
    private ProductDAO productDAO;
    private FlashSaleDAO flashSaleDAO;
    private OrderDAO orderDAO;

    private List<CartItem> cartItems;
    private List<Product> productsInCart;
    private double currentSubtotalPrice = 0; // Đổi tên thành subtotal

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

        // Ánh xạ các TextView mới
        subtotalAmountTextView = findViewById(R.id.subtotal_amount_text_view);
        vatAmountTextView = findViewById(R.id.vat_amount_text_view);
        finalTotalAmountTextView = findViewById(R.id.total_amount_text_view); // Đây là total_amount_text_view cũ của bạn

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
            finish();
            return;
        }

        // Tải thông tin người dùng và giỏ hàng
        loadUserInfo();
        loadCartItemsAndCalculateTotal(); // Phương thức này sẽ tính toán và cập nhật các TextView

        setupOrderSummaryRecyclerView();

        // Xử lý nút Xác nhận đơn hàng
        confirmOrderButton.setOnClickListener(v -> confirmOrder());
    }

    private void loadUserInfo() {
        User user = userDAO.getUserById(userId);
        if (user != null) {
            nameEditText.setText(user.getFullName());
            phoneEditText.setText(user.getPhone());
            // Đối với địa chỉ, nếu User model của bạn đã có trường 'address', bạn có thể điền vào đây
            addressEditText.setText(user.getAddress()); // Giả sử user.getAddress() có sẵn
        } else {
            Toast.makeText(this, "Không thể tải thông tin người dùng. Vui lòng điền thủ công.", Toast.LENGTH_SHORT).show();
        }
    }

    private void loadCartItemsAndCalculateTotal() {
        cartItems = cartDAO.getCartItems(userId);
        productsInCart = new ArrayList<>();
        currentSubtotalPrice = 0; // Reset tổng tiền hàng

        if (cartItems.isEmpty()) {
            Toast.makeText(this, "Giỏ hàng trống. Không thể thanh toán.", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        for (CartItem item : cartItems) {
            Product p = productDAO.getProductById(item.getProductId());
            if (p != null) {
                productsInCart.add(p);
                FlashSale flashSale = flashSaleDAO.getFlashSaleByProductId(p.getId());
                if (flashSale != null) {
                    currentSubtotalPrice += flashSale.getSalePrice() * item.getQuantity();
                } else {
                    currentSubtotalPrice += p.getPrice() * item.getQuantity();
                }
            }
        }

        // --- Cập nhật các TextView tổng tiền ---
        double vatAmount = currentSubtotalPrice * VAT_PERCENTAGE;
        double finalTotalAmount = currentSubtotalPrice + vatAmount;

        subtotalAmountTextView.setText(String.format(Locale.getDefault(), "%,.0f đ", currentSubtotalPrice));
        vatAmountTextView.setText(String.format(Locale.getDefault(), "%,.0f đ", vatAmount));
        finalTotalAmountTextView.setText(String.format(Locale.getDefault(), "%,.0f đ", finalTotalAmount));
    }

    private void setupOrderSummaryRecyclerView() {
        orderSummaryRecyclerView.setLayoutManager(new LinearLayoutManager(this));
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

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        String orderDate = sdf.format(new Date());

        double finalTotalAmount = currentSubtotalPrice * (1 + VAT_PERCENTAGE);

        Order newOrder = new Order(
                userId,
                orderDate,
                finalTotalAmount,
                customerName,
                customerAddress,
                customerPhone,
                paymentMethod,
                "Đang chờ xác nhận"
        );

        long orderId = orderDAO.createOrder(newOrder, cartItems);

        if (orderId != -1) {
            if (paymentMethod.equals("Chuyển khoản ngân hàng")) {
                // Nếu là chuyển khoản, chuyển sang ActivityBankTransfer
                Intent bankIntent = new Intent(ActivityCheckOut.this, ActivityBankTransfer.class);
                bankIntent.putExtra("amount", finalTotalAmount);
                bankIntent.putExtra("orderId", orderId);
                startActivity(bankIntent);
                finish();
                return;
            }
            Toast.makeText(this, "Đặt hàng thành công! Mã đơn hàng của bạn: " + orderId, Toast.LENGTH_LONG).show();
            cartDAO.clearCart(userId);
            Intent intent = new Intent(ActivityCheckOut.this, MainActivity.class);
            intent.putExtra("userId", userId);
            startActivity(intent);
            finish();
        } else {
            Toast.makeText(this, "Đặt hàng thất bại. Vui lòng thử lại!", Toast.LENGTH_LONG).show();
        }
    }

    // Adapter nội bộ đơn giản cho RecyclerView hiển thị tóm tắt đơn hàng
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
            Product product = productDAO.getProductById(item.getProductId());

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