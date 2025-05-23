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
    // Khởi tạo các DAO nội bộ với dbHelper để chúng có thể chia sẻ kết nối DB
    private ProductDAO productDAO_internal;
    private FlashSaleDAO flashSaleDAO_internal;
    private UserDAO userDAO_internal;

    public OrderDAO(Context context) {
        dbHelper = new DatabaseHelper(context);
        productDAO_internal = new ProductDAO(dbHelper); // <-- KHỞI TẠO VỚI DBHELPER
        flashSaleDAO_internal = new FlashSaleDAO(dbHelper); // <-- KHỞI TẠO VỚI DBHELPER
        userDAO_internal = new UserDAO(context); // UserDAO vẫn dùng Context vì nó không gọi lại ProductDAO/FlashSaleDAO trong transaction này
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
            db = dbHelper.getWritableDatabase(); // Mở DB ở chế độ ghi
            Log.d(TAG, "Database opened for writing. Starting transaction.");
            db.beginTransaction(); // Bắt đầu transaction

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

                    // GỌI PHƯƠNG THỨC QUÁ TẢI TRONG ProductDAO và FlashSaleDAO, TRUYỀN CÙNG KẾT NỐI DB (db)
                    Product product = productDAO_internal.getProductById(item.getProductId(), db); // <-- SỬA Ở ĐÂY
                    if (product != null) {
                        double unitPrice;
                        FlashSale flashSale = flashSaleDAO_internal.getFlashSaleByProductId(product.getId(), db); // <-- SỬA Ở ĐÂY
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
                        // GỌI PHƯƠNG THỨC QUÁ TẢI
                        int rowsAffected = productDAO_internal.updateProductStock(product.getId(), newStock, db); // <-- SỬA Ở ĐÂY
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

    public List<Order> getOrdersByUserId(int userId) {
        List<Order> orderList = new ArrayList<>();
        SQLiteDatabase db = null;
        Cursor cursor = null;
        try {
            db = dbHelper.getReadableDatabase();
            Log.d(TAG, "getOrdersByUserId: Database opened for reading.");
            cursor = db.query("orders", null, "userId=?", new String[]{String.valueOf(userId)}, null, null, "orderDate DESC");

            if (cursor != null && cursor.moveToFirst()) {
                do {
                    Order order = new Order(
                            cursor.getInt(cursor.getColumnIndexOrThrow("id")),
                            cursor.getInt(cursor.getColumnIndexOrThrow("userId")),
                            cursor.getString(cursor.getColumnIndexOrThrow("orderDate")),
                            cursor.getDouble(cursor.getColumnIndexOrThrow("totalAmount")),
                            cursor.getString(cursor.getColumnIndexOrThrow("customerName")),
                            cursor.getString(cursor.getColumnIndexOrThrow("customerAddress")),
                            cursor.getString(cursor.getColumnIndexOrThrow("customerPhone")),
                            cursor.getString(cursor.getColumnIndexOrThrow("paymentMethod")),
                            cursor.getString(cursor.getColumnIndexOrThrow("status"))
                    );
                    orderList.add(order);
                } while (cursor.moveToNext());
                Log.d(TAG, "getOrdersByUserId: Found " + orderList.size() + " orders for userId: " + userId);
            } else {
                Log.d(TAG, "getOrdersByUserId: No orders found for userId: " + userId);
            }
        } catch (Exception e) {
            Log.e(TAG, "Exception in getOrdersByUserId: " + e.getMessage(), e);
        } finally {
            if (cursor != null) cursor.close();
            if (db != null && db.isOpen()) {
                db.close();
                Log.d(TAG, "getOrdersByUserId: Database connection closed.");
            }
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
            cursor = db.query("order_details", null, "orderId=?", new String[]{String.valueOf(orderId)}, null, null, null);

            if (cursor != null && cursor.moveToFirst()) {
                do {
                    OrderDetail detail = new OrderDetail(
                            cursor.getInt(cursor.getColumnIndexOrThrow("id")),
                            cursor.getInt(cursor.getColumnIndexOrThrow("orderId")),
                            cursor.getInt(cursor.getColumnIndexOrThrow("productId")),
                            cursor.getInt(cursor.getColumnIndexOrThrow("quantity")),
                            cursor.getDouble(cursor.getColumnIndexOrThrow("unitPrice"))
                    );
                    detailList.add(detail);
                } while (cursor.moveToNext());
                Log.d(TAG, "getOrderDetailsByOrderId: Found " + detailList.size() + " details for orderId: " + orderId);
            } else {
                Log.d(TAG, "getOrderDetailsByOrderId: No details found for orderId: " + orderId);
            }
        } catch (Exception e) {
            Log.e(TAG, "Exception in getOrderDetailsByOrderId: " + e.getMessage(), e);
        } finally {
            if (cursor != null) cursor.close();
            if (db != null && db.isOpen()) {
                db.close();
                Log.d(TAG, "getOrderDetailsByOrderId: Database connection closed.");
            }
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
            cursor = db.query("orders", null, "id=?", new String[]{String.valueOf(orderId)}, null, null, null);
            if (cursor != null && cursor.moveToFirst()) {
                order = new Order(
                        cursor.getInt(cursor.getColumnIndexOrThrow("id")),
                        cursor.getInt(cursor.getColumnIndexOrThrow("userId")),
                        cursor.getString(cursor.getColumnIndexOrThrow("orderDate")),
                        cursor.getDouble(cursor.getColumnIndexOrThrow("totalAmount")),
                        cursor.getString(cursor.getColumnIndexOrThrow("customerName")),
                        cursor.getString(cursor.getColumnIndexOrThrow("customerAddress")),
                        cursor.getString(cursor.getColumnIndexOrThrow("customerPhone")),
                        cursor.getString(cursor.getColumnIndexOrThrow("paymentMethod")),
                        cursor.getString(cursor.getColumnIndexOrThrow("status"))
                );
                Log.d(TAG, "getOrderById: Order found with ID: " + orderId);
            } else {
                Log.d(TAG, "getOrderById: No order found for ID: " + orderId);
            }
        } catch (Exception e) {
            Log.e(TAG, "Exception in getOrderById: " + e.getMessage(), e);
        } finally {
            if (cursor != null) cursor.close();
            if (db != null && db.isOpen()) {
                db.close();
                Log.d(TAG, "getOrderById: Database connection closed.");
            }
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
            cursor = db.query("orders", null, null, null, null, null, "orderDate DESC");

            if (cursor != null && cursor.moveToFirst()) {
                do {
                    Order order = new Order(
                            cursor.getInt(cursor.getColumnIndexOrThrow("id")),
                            cursor.getInt(cursor.getColumnIndexOrThrow("userId")),
                            cursor.getString(cursor.getColumnIndexOrThrow("orderDate")),
                            cursor.getDouble(cursor.getColumnIndexOrThrow("totalAmount")),
                            cursor.getString(cursor.getColumnIndexOrThrow("customerName")),
                            cursor.getString(cursor.getColumnIndexOrThrow("customerAddress")),
                            cursor.getString(cursor.getColumnIndexOrThrow("customerPhone")),
                            cursor.getString(cursor.getColumnIndexOrThrow("paymentMethod")),
                            cursor.getString(cursor.getColumnIndexOrThrow("status"))
                    );
                    orderList.add(order);
                } while (cursor.moveToNext());
                Log.d(TAG, "getAllOrders: Found " + orderList.size() + " orders.");
            } else {
                Log.d(TAG, "getAllOrders: No orders found.");
            }
        } catch (Exception e) {
            Log.e(TAG, "Exception in getAllOrders: " + e.getMessage(), e);
        } finally {
            if (cursor != null) cursor.close();
            if (db != null && db.isOpen()) {
                db.close();
                Log.d(TAG, "getAllOrders: Database connection closed.");
            }
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
            cursor = db.query("orders", null, "status=?", new String[]{status}, null, null, "orderDate DESC");

            if (cursor != null && cursor.moveToFirst()) {
                do {
                    Order order = new Order(
                            cursor.getInt(cursor.getColumnIndexOrThrow("id")),
                            cursor.getInt(cursor.getColumnIndexOrThrow("userId")),
                            cursor.getString(cursor.getColumnIndexOrThrow("orderDate")),
                            cursor.getDouble(cursor.getColumnIndexOrThrow("totalAmount")),
                            cursor.getString(cursor.getColumnIndexOrThrow("customerName")),
                            cursor.getString(cursor.getColumnIndexOrThrow("customerAddress")),
                            cursor.getString(cursor.getColumnIndexOrThrow("customerPhone")),
                            cursor.getString(cursor.getColumnIndexOrThrow("paymentMethod")),
                            cursor.getString(cursor.getColumnIndexOrThrow("status"))
                    );
                    orderList.add(order);
                } while (cursor.moveToNext());
                Log.d(TAG, "getOrdersByStatus: Found " + orderList.size() + " orders for status: " + status);
            } else {
                Log.d(TAG, "getOrdersByStatus: No orders found for status: " + status);
            }
        } catch (Exception e) {
            Log.e(TAG, "Exception in getOrdersByStatus: " + e.getMessage(), e);
        } finally {
            if (cursor != null) cursor.close();
            if (db != null && db.isOpen()) {
                db.close();
                Log.d(TAG, "getOrdersByStatus: Database connection closed.");
            }
        }
        return orderList;
    }

    public boolean updateOrderStatus(int orderId, String newStatus) {
        SQLiteDatabase db = null;
        int rowsAffected = 0;
        try {
            db = dbHelper.getWritableDatabase();
            ContentValues values = new ContentValues();
            values.put("status", newStatus);
            rowsAffected = db.update("orders", values, "id=?", new String[]{String.valueOf(orderId)});
            Log.d(TAG, "updateOrderStatus: Order ID " + orderId + " updated to status '" + newStatus + "'. Rows affected: " + rowsAffected);
        } catch (Exception e) {
            Log.e(TAG, "Exception in updateOrderStatus: " + e.getMessage(), e);
        } finally {
            if (db != null && db.isOpen()) {
                db.close();
                Log.d(TAG, "updateOrderStatus: Database connection closed.");
            }
        }
        return rowsAffected > 0;
    }
}