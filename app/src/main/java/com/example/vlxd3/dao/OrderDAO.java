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

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class OrderDAO {
    private DatabaseHelper dbHelper;
    // DAO instances for internal use should not be re-initialized in every method.
    // They should be initialized once in the constructor.
    private ProductDAO productDAO;
    private FlashSaleDAO flashSaleDAO;

    public OrderDAO(Context context) {
        dbHelper = new DatabaseHelper(context);
        // Initialize child DAOs once
        productDAO = new ProductDAO(context);
        flashSaleDAO = new FlashSaleDAO(context);
    }

    public long createOrder(Order order, List<CartItem> cartItems) {
        SQLiteDatabase db = null; // Khởi tạo db là null
        long orderId = -1;

        try {
            db = dbHelper.getWritableDatabase(); // Lấy kết nối ghi
            db.beginTransaction(); // Bắt đầu transaction

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
                    // Chú ý: ProductDAO và FlashSaleDAO được khởi tạo ở constructor của OrderDAO
                    // Nên không cần gọi getReadableDatabase/getWritableDatabase lại ở đây cho chúng
                    // vì chúng có context riêng và quản lý dbHelper của chúng.
                    // Vấn đề khóa database thường xảy ra khi cùng một dbHelper được mở nhiều lần
                    // mà không đóng.
                    Product product = productDAO.getProductById(item.getProductId());
                    if (product != null) {
                        double unitPrice;
                        FlashSale flashSale = flashSaleDAO.getFlashSaleByProductId(product.getId());
                        if (flashSale != null) {
                            unitPrice = flashSale.getSalePrice();
                        } else {
                            unitPrice = product.getPrice();
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
                        productDAO.updateProductStock(product.getId(), newStock);
                        Log.d("OrderDAO", "Updated stock for Product " + product.getId() + " to " + newStock);
                    }
                }
                db.setTransactionSuccessful(); // Đánh dấu transaction thành công
            } else {
                Log.e("OrderDAO", "Failed to create order, orderId is -1.");
            }
        } catch (Exception e) {
            Log.e("OrderDAO", "Error creating order: " + e.getMessage());
            orderId = -1;
        } finally {
            if (db != null) {
                db.endTransaction(); // Kết thúc transaction
                db.close(); // Đóng database
            }
        }
        return orderId;
    }

    public List<Order> getOrdersByUserId(int userId) {
        List<Order> orderList = new ArrayList<>();
        SQLiteDatabase db = null;
        Cursor cursor = null;
        try {
            db = dbHelper.getReadableDatabase();
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
            }
        } catch (Exception e) {
            Log.e("OrderDAO", "Error getting orders by user ID: " + e.getMessage());
        } finally {
            if (cursor != null) cursor.close();
            if (db != null) db.close();
        }
        return orderList;
    }

    public List<OrderDetail> getOrderDetailsByOrderId(int orderId) {
        List<OrderDetail> detailList = new ArrayList<>();
        SQLiteDatabase db = null;
        Cursor cursor = null;
        try {
            db = dbHelper.getReadableDatabase();
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
            }
        } catch (Exception e) {
            Log.e("OrderDAO", "Error getting order details by order ID: " + e.getMessage());
        } finally {
            if (cursor != null) cursor.close();
            if (db != null) db.close();
        }
        return detailList;
    }

    public Order getOrderById(int orderId) {
        SQLiteDatabase db = null;
        Cursor cursor = null;
        Order order = null;
        try {
            db = dbHelper.getReadableDatabase();
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
            }
        } catch (Exception e) {
            Log.e("OrderDAO", "Error getting order by ID: " + e.getMessage());
        } finally {
            if (cursor != null) cursor.close();
            if (db != null) db.close();
        }
        return order;
    }
}