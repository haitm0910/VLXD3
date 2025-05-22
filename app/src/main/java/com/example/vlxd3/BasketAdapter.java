// File: BasketAdapter.java

package com.example.vlxd3;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.vlxd3.dao.CartDAO;
import com.example.vlxd3.dao.FlashSaleDAO;
import com.example.vlxd3.dao.ProductDAO; // Thêm import ProductDAO để lấy stock
import com.example.vlxd3.model.CartItem;
import com.example.vlxd3.model.FlashSale;
import com.example.vlxd3.model.Product;

import java.util.List;
import java.util.Locale;

public class BasketAdapter extends BaseAdapter {
    private Context context;
    private List<CartItem> cartItems;
    private List<Product> products; // Danh sách các Product tương ứng với cartItems
    private FlashSaleDAO flashSaleDAO;
    private CartDAO cartDAO;
    private ProductDAO productDAO; // Khai báo ProductDAO để lấy thông tin stock
    private CartUpdateListener listener;

    public BasketAdapter(Context context, List<CartItem> cartItems, List<Product> products, FlashSaleDAO flashSaleDAO, CartUpdateListener listener) {
        this.context = context;
        this.cartItems = cartItems;
        this.products = products;
        this.flashSaleDAO = flashSaleDAO;
        this.cartDAO = new CartDAO(context);
        this.productDAO = new ProductDAO(context); // Khởi tạo ProductDAO
        this.listener = listener;
    }

    @Override
    public int getCount() {
        return cartItems.size();
    }

    @Override
    public Object getItem(int position) {
        return cartItems.get(position);
    }

    @Override
    public long getItemId(int position) {
        return cartItems.get(position).getId();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.item_basket_product, parent, false);
        }
        TextView nameTextView = convertView.findViewById(R.id.product_name);
        TextView priceTextView = convertView.findViewById(R.id.product_price);
        TextView quantityTextView = convertView.findViewById(R.id.quantity_text);
        ImageView image = convertView.findViewById(R.id.product_image);
        ImageView removeItemButton = convertView.findViewById(R.id.remove_item);

        ImageView quantityMinusButton = convertView.findViewById(R.id.quantity_minus);
        ImageView quantityPlusButton = convertView.findViewById(R.id.quantity_plus); // Sửa lỗi potential ID here

        CartItem item = cartItems.get(position);
        // Lấy Product trực tiếp từ DAO thay vì từ list 'products'
        // để đảm bảo thông tin stock là mới nhất và chính xác
        Product product = productDAO.getProductById(item.getProductId());


        if (product != null) { // Đảm bảo sản phẩm tồn tại
            nameTextView.setText(product.getName());
            quantityTextView.setText("x" + item.getQuantity());

            FlashSale flashSale = flashSaleDAO.getFlashSaleByProductId(product.getId());

            if (flashSale != null) {
                String salePriceFormatted = String.format(Locale.getDefault(), "%,.0f đ", flashSale.getSalePrice());
                priceTextView.setText(salePriceFormatted);
                priceTextView.setTextColor(Color.RED);
                priceTextView.setTypeface(null, Typeface.BOLD);
            } else {
                String normalPriceFormatted = String.format(Locale.getDefault(), "%,.0f đ", product.getPrice());
                priceTextView.setText(normalPriceFormatted);
                priceTextView.setTextColor(Color.BLACK);
                priceTextView.setTypeface(null, Typeface.NORMAL);
            }

            // image.setImageResource(...) // Để bạn tự thêm ảnh sau

            // Xử lý sự kiện khi nhấn nút xóa sản phẩm
            removeItemButton.setOnClickListener(v -> {
                int result = cartDAO.removeFromCart(item.getUserId(), item.getProductId());
                if (result > 0) {
                    // Xóa sản phẩm khỏi danh sách hiện tại của Adapter
                    cartItems.remove(position);
                    products.remove(position); // Cũng xóa khỏi list products để đồng bộ
                    notifyDataSetChanged(); // Cập nhật lại ListView

                    if (listener != null) {
                        listener.onCartUpdated(); // Thông báo cho Activity để cập nhật tổng tiền và trạng thái giỏ hàng
                    }
                    Toast.makeText(context, "Đã xóa sản phẩm khỏi giỏ hàng!", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(context, "Xóa sản phẩm thất bại!", Toast.LENGTH_SHORT).show();
                }
            });

            // Xử lý sự kiện khi nhấn nút giảm số lượng
            if (quantityMinusButton != null) {
                quantityMinusButton.setOnClickListener(v -> {
                    int currentQty = item.getQuantity();
                    if (currentQty > 1) {
                        item.setQuantity(currentQty - 1);
                        int updateResult = cartDAO.updateQuantity(item.getUserId(), item.getProductId(), item.getQuantity());
                        if (updateResult > 0) {
                            notifyDataSetChanged();
                            if (listener != null) {
                                listener.onCartUpdated();
                            }
                        } else {
                            Toast.makeText(context, "Cập nhật số lượng thất bại!", Toast.LENGTH_SHORT).show();
                        }
                    } else if (currentQty == 1) {
                        // Nếu số lượng là 1 và nhấn giảm, coi như xóa sản phẩm
                        int removeResult = cartDAO.removeFromCart(item.getUserId(), item.getProductId());
                        if (removeResult > 0) {
                            cartItems.remove(position);
                            products.remove(position); // Cũng xóa khỏi list products để đồng bộ
                            notifyDataSetChanged();
                            if (listener != null) {
                                listener.onCartUpdated();
                            }
                            Toast.makeText(context, "Đã xóa sản phẩm khỏi giỏ hàng!", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(context, "Xóa sản phẩm thất bại!", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }

            // Xử lý sự kiện khi nhấn nút tăng số lượng
            if (quantityPlusButton != null) {
                quantityPlusButton.setOnClickListener(v -> {
                    int currentQty = item.getQuantity();
                    // Lấy lại thông tin product để đảm bảo stock là mới nhất
                    Product latestProduct = productDAO.getProductById(item.getProductId());
                    if (latestProduct != null && currentQty < latestProduct.getStock()) { // Kiểm tra với số lượng tồn kho
                        item.setQuantity(currentQty + 1);
                        int updateResult = cartDAO.updateQuantity(item.getUserId(), item.getProductId(), item.getQuantity());
                        if (updateResult > 0) {
                            notifyDataSetChanged();
                            if (listener != null) {
                                listener.onCartUpdated();
                            }
                        } else {
                            Toast.makeText(context, "Cập nhật số lượng thất bại!", Toast.LENGTH_SHORT).show();
                        }
                    } else if (latestProduct != null && currentQty >= latestProduct.getStock()) {
                        Toast.makeText(context, "Số lượng đã đạt tối đa trong kho: " + latestProduct.getStock() + "!", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(context, "Không thể xác định tồn kho của sản phẩm!", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        } else {
            // Xử lý trường hợp không tìm thấy Product (có thể do lỗi database hoặc dữ liệu không đồng bộ)
            nameTextView.setText("Sản phẩm không tồn tại");
            priceTextView.setText("0 đ");
            quantityTextView.setText("x0");
            image.setImageResource(R.drawable.logo); // Đặt ảnh mặc định
            removeItemButton.setVisibility(View.GONE); // Ẩn nút xóa nếu sản phẩm không tồn tại
            if(quantityMinusButton != null) quantityMinusButton.setVisibility(View.GONE);
            if(quantityPlusButton != null) quantityPlusButton.setVisibility(View.GONE);
        }

        return convertView;
    }
}