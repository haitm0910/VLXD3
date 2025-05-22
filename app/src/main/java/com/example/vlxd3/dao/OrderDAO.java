// File: OrderDAO.java
package com.example.vlxd3.dao;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log; // Import Log cho debug

import com.example.vlxd3.database.DatabaseHelper;
import com.example.vlxd3.model.Order;
import com.example.vlxd3.model.CartItem; // Import CartItem
import com.example.vlxd3.model.OrderDetail;
import com.example.vlxd3.model.Product; // Import Product
import com.example.vlxd3.model.FlashSale; // Import FlashSale

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class OrderDAO {
    private DatabaseHelper dbHelper;
    private ProductDAO productDAO;
    private FlashSaleDAO flashSaleDAO;

    public OrderDAO(Context context) {
        dbHelper = new DatabaseHelper(context);
        productDAO = new ProductDAO(context);
        flashSaleDAO = new FlashSaleDAO(context);
    }

    public long createOrder(Order order, List<CartItem> cartItems) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        long orderId = -1; // Khởi tạo orderId

        try {
            // 1. Thêm đơn hàng vào bảng 'orders'
            ContentValues orderValues = new ContentValues();
            orderValues.put("userId", order.getUserId());
            orderValues.put("orderDate", order.getOrderDate());
            orderValues.put("totalAmount", order.getTotalAmount());
            orderValues.put("customerName", order.getCustomerName());
            orderValues.put("customerAddress", order.getCustomerAddress());
            orderValues.put("customerPhone", order.getCustomerPhone());
            orderValues.put("paymentMethod", order.getPaymentMethod());
            orderValues.put("status", order.getStatus());

            orderId = db.insert("orders", null, orderValues);
            Log.d("OrderDAO", "Order created with ID: " + orderId);

            if (orderId != -1) {
                // 2. Thêm chi tiết đơn hàng vào bảng 'order_details' và cập nhật tồn kho
                for (CartItem item : cartItems) {
                    Product product = productDAO.getProductById(item.getProductId());
                    if (product != null) {
                        double unitPrice;
                        FlashSale flashSale = flashSaleDAO.getFlashSaleByProductId(product.getId());
                        if (flashSale != null) {
                            unitPrice = flashSale.getSalePrice(); // Lấy giá sale
                        } else {
                            unitPrice = product.getPrice(); // Lấy giá gốc
                        }

                        ContentValues detailValues = new ContentValues();
                        detailValues.put("orderId", orderId);
                        detailValues.put("productId", item.getProductId());
                        detailValues.put("quantity", item.getQuantity());
                        detailValues.put("unitPrice", unitPrice);

                        long detailId = db.insert("order_details", null, detailValues);
                        Log.d("OrderDAO", "OrderDetail created with ID: " + detailId + " for Order ID: " + orderId);

                        // 3. Cập nhật số lượng tồn kho của sản phẩm
                        int newStock = product.getStock() - item.getQuantity();
                        productDAO.updateProductStock(product.getId(), newStock); // Cần thêm phương thức này vào ProductDAO
                        Log.d("OrderDAO", "Updated stock for Product " + product.getId() + " to " + newStock);
                    }
                }
            } else {
                Log.e("OrderDAO", "Failed to create order, orderId is -1.");
            }
        } catch (Exception e) {
            Log.e("OrderDAO", "Error creating order: " + e.getMessage());
            orderId = -1; // Đảm bảo trả về -1 nếu có lỗi
        } finally {
            db.close();
        }
        return orderId;
    }

    // Các phương thức khác có thể thêm vào: getOrderById, getOrderDetailsByOrderId, v.v.
}