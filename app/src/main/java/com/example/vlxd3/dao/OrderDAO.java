// File: OrderDAO.java

package com.example.vlxd3.dao;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.example.vlxd3.database.DatabaseHelper;
import com.example.vlxd3.model.Order;
import com.example.vlxd3.model.CartItem;
import com.example.vlxd3.model.OrderDetail;
import com.example.vlxd3.model.Product;
import com.example.vlxd3.model.FlashSale;
import com.example.vlxd3.model.User;

import java.util.ArrayList;
import java.util.List;

public class OrderDAO {
    private static final String TAG = "OrderDAO";
    private DatabaseHelper dbHelper;
    private ProductDAO productDAO_internal; // Sẽ dùng ProductDAO(Context)
    private FlashSaleDAO flashSaleDAO_internal; // Sẽ dùng FlashSaleDAO(Context)
    private UserDAO userDAO_internal; // Sẽ dùng UserDAO(Context)

    public OrderDAO(Context context) {
        dbHelper = new DatabaseHelper(context);
        // Khởi tạo các DAO nội bộ với Context
        productDAO_internal = new ProductDAO(context);
        flashSaleDAO_internal = new FlashSaleDAO(context);
        userDAO_internal = new UserDAO(context);
        Log.d(TAG, "OrderDAO initialized.");
    }

    public long createOrder(Order order, List<CartItem> cartItems) {
        SQLiteDatabase db = null;
        long orderId = -1;

        Log.d(TAG, "Attempting to create a new order.");
        Log.d(TAG, "Order details: UserID=" + order.getUserId() + ", TotalAmount=" + order.getTotalAmount());
        Log.d(TAG, "Cart items count for this order: " + (cartItems != null ? cartItems.size() : "null list"));

        if (cartItems == null || cartItems.isEmpty()) {
            Log.e(TAG, "Cart items list is null or empty. Cannot create order details.");
            return -1;
        }

        try {
            db = dbHelper.getWritableDatabase();
            Log.d(TAG, "Database opened for writing. Starting transaction.");
            db.beginTransaction();

            // 1. Thêm đơn hàng tổng quát vào bảng 'orders'
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
            Log.d(TAG, "INSERT into 'orders' result (orderId): " + orderId);

            if (orderId != -1) {
                Log.d(TAG, "Order header created successfully. Processing order details for Order ID: " + orderId);
                Log.d(TAG, "Number of cart items to process: " + cartItems.size());

                // 2. Thêm chi tiết từng sản phẩm vào bảng 'order_details' và cập nhật tồn kho
                for (CartItem item : cartItems) {
                    Log.d(TAG, "Processing cart item productId: " + item.getProductId() + ", quantity: " + item.getQuantity());

                    // GỌI PHƯƠNG THỨC KHÔNG QUÁ TẢI (chúng sẽ tự mở/đóng DB của riêng chúng)
                    Product product = productDAO_internal.getProductById(item.getProductId()); // <-- SỬA Ở ĐÂY
                    if (product != null) {
                        double unitPrice;
                        // GỌI PHƯƠNG THỨC KHÔNG QUÁ TẢI
                        FlashSale flashSale = flashSaleDAO_internal.getFlashSaleByProductId(product.getId()); // <-- SỬA Ở ĐÂY
                        if (flashSale != null) {
                            unitPrice = flashSale.getSalePrice();
                            Log.d(TAG, "  -> Product " + product.getId() + " is in Flash Sale. Unit Price: " + unitPrice);
                        } else {
                            unitPrice = product.getPrice();
                            Log.d(TAG, "  -> Product " + product.getId() + " is normal price. Unit Price: " + unitPrice);
                        }

                        ContentValues detailValues = new ContentValues();
                        detailValues.put("orderId", orderId);
                        detailValues.put("productId", item.getProductId());
                        detailValues.put("quantity", item.getQuantity());
                        detailValues.put("unitPrice", unitPrice);

                        long detailId = db.insert("order_details", null, detailValues);
                        if (detailId == -1) {
                            Log.e(TAG, "  -> FAILED to insert OrderDetail for ProductID: " + item.getProductId() + " (returned -1).");
                            // Giữ nguyên rollback nếu insert detail thất bại
                            db.endTransaction();
                            Log.e(TAG, "Transaction rolled back due to failed OrderDetail insert.");
                            return -1;
                        } else {
                            Log.d(TAG, "  -> OrderDetail for ProductID: " + item.getProductId() + " inserted successfully. Detail ID: " + detailId);
                        }

                        // 3. Cập nhật số lượng tồn kho của sản phẩm
                        int newStock = product.getStock() - item.getQuantity();
                        // GỌI PHƯƠNG THỨC KHÔNG QUÁ TẢI
                        int rowsAffected = productDAO_internal.updateProductStock(product.getId(), newStock); // <-- SỬA Ở ĐÂY
                        if (rowsAffected <= 0) {
                            Log.e(TAG, "  -> FAILED to update stock for ProductID: " + product.getId() + ". Rows affected: " + rowsAffected);
                            // Giữ nguyên rollback nếu update stock thất bại
                            db.endTransaction();
                            Log.e(TAG, "Transaction rolled back due to failed stock update.");
                            return -1;
                        } else {
                            Log.d(TAG, "  -> Stock updated for ProductID: " + product.getId() + ". New stock: " + newStock);
                        }

                    } else {
                        Log.e(TAG, "  -> Product with ID: " + item.getProductId() + " NOT FOUND in database while processing cart item.");
                        // Giữ nguyên rollback nếu sản phẩm không tìm thấy
                        db.endTransaction();
                        Log.e(TAG, "Transaction rolled back because a product was not found.");
                        return -1;
                    }
                }
                db.setTransactionSuccessful(); // Đánh dấu giao dịch thành công chỉ khi tất cả detail được chèn
                Log.d(TAG, "Transaction successful.");
            } else {
                Log.e(TAG, "Failed to create order header. orderId is -1.");
            }
        } catch (Exception e) {
            Log.e(TAG, "EXCEPTION during order creation: " + e.getMessage(), e); // Log full stack trace
            orderId = -1;
        } finally {
            if (db != null && db.inTransaction()) { // Chỉ kết thúc transaction nếu nó đang hoạt động
                db.endTransaction();
                Log.d(TAG, "Transaction ended.");
            }
            if (db != null && db.isOpen()) { // Chỉ đóng nếu nó đang mở
                db.close();
                Log.d(TAG, "Database connection closed.");
            }
        }
        Log.d(TAG, "createOrder finished. Returning orderId: " + orderId);
        return orderId;
    }

    // CÁC PHƯƠNG THỨC DƯỚI ĐÂY (getOrdersByUserId, getOrderDetailsByOrderId, getOrderById, getAllOrders, getOrdersByStatus, updateOrderStatus)
    // ĐẢM BẢO NẰM NGOÀI PHƯƠNG THỨC createOrder() và giữ nguyên như đã sửa ở lần trước, với try-finally và đóng db/cursor.
    // Tôi sẽ chỉ dán lại phần đầu của mỗi phương thức để nhắc vị trí.

    public List<Order> getOrdersByUserId(int userId) {
        List<Order> orderList = new ArrayList<>();
        SQLiteDatabase db = null;
        Cursor cursor = null;
        try {
            db = dbHelper.getReadableDatabase();
            Log.d(TAG, "getOrdersByUserId: Database opened for reading.");
            // ... (phần còn lại của phương thức)
            if (cursor != null) cursor.close();
            if (db != null && db.isOpen()) db.close();
        } catch (Exception e) {
            Log.e(TAG, "Exception in getOrdersByUserId: " + e.getMessage(), e);
        } finally {
            // ... (finally block)
        }
        return orderList;
    }

    public List<OrderDetail> getOrderDetailsByOrderId(int orderId) {
        List<OrderDetail> detailList = new ArrayList<>();
        SQLiteDatabase db = null;
        Cursor cursor = null;
        try {
            db = dbHelper.getReadableDatabase();
            Log.d(TAG, "getOrderDetailsByOrderId: Database opened for reading.");
            // ... (phần còn lại của phương thức)
            if (cursor != null) cursor.close();
            if (db != null && db.isOpen()) db.close();
        } catch (Exception e) {
            Log.e(TAG, "Exception in getOrderDetailsByOrderId: " + e.getMessage(), e);
        } finally {
            // ... (finally block)
        }
        return detailList;
    }

    public Order getOrderById(int orderId) {
        SQLiteDatabase db = null;
        Cursor cursor = null;
        Order order = null;
        try {
            db = dbHelper.getReadableDatabase();
            Log.d(TAG, "getOrderById: Database opened for reading.");
            // ... (phần còn lại của phương thức)
            if (cursor != null) cursor.close();
            if (db != null && db.isOpen()) db.close();
        } catch (Exception e) {
            Log.e(TAG, "Exception in getOrderById: " + e.getMessage(), e);
        } finally {
            // ... (finally block)
        }
        return order;
    }

    public List<Order> getAllOrders() {
        List<Order> orderList = new ArrayList<>();
        SQLiteDatabase db = null;
        Cursor cursor = null;
        try {
            db = dbHelper.getReadableDatabase();
            Log.d(TAG, "getAllOrders: Database opened for reading.");
            // ... (phần còn lại của phương thức)
            if (cursor != null) cursor.close();
            if (db != null && db.isOpen()) db.close();
        } catch (Exception e) {
            Log.e(TAG, "Exception in getAllOrders: " + e.getMessage(), e);
        } finally {
            // ... (finally block)
        }
        return orderList;
    }

    public List<Order> getOrdersByStatus(String status) {
        List<Order> orderList = new ArrayList<>();
        SQLiteDatabase db = null;
        Cursor cursor = null;
        try {
            db = dbHelper.getReadableDatabase();
            Log.d(TAG, "getOrdersByStatus: Database opened for reading. Status: " + status);
            // ... (phần còn lại của phương thức)
            if (cursor != null) cursor.close();
            if (db != null && db.isOpen()) db.close();
        } catch (Exception e) {
            Log.e(TAG, "Exception in getOrdersByStatus: " + e.getMessage(), e);
        } finally {
            // ... (finally block)
        }
        return orderList;
    }

    public boolean updateOrderStatus(int orderId, String newStatus) {
        SQLiteDatabase db = null;
        int rowsAffected = 0;
        try {
            db = dbHelper.getWritableDatabase();
            // ... (phần còn lại của phương thức)
            if (db != null && db.isOpen()) db.close();
        } catch (Exception e) {
            Log.e(TAG, "Exception in updateOrderStatus: " + e.getMessage(), e);
        } finally {
            // ... (finally block)
        }
        return rowsAffected > 0;
    }
}