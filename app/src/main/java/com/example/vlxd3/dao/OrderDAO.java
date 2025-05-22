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

import java.util.ArrayList;
import java.util.List;

public class OrderDAO {
    private static final String TAG = "OrderDAO";
    private DatabaseHelper dbHelper;
    private ProductDAO productDAO_internal;
    private FlashSaleDAO flashSaleDAO_internal;

    public OrderDAO(Context context) {
        dbHelper = new DatabaseHelper(context);
        productDAO_internal = new ProductDAO(dbHelper); // Khởi tạo với dbHelper để chia sẻ kết nối
        flashSaleDAO_internal = new FlashSaleDAO(dbHelper); // Khởi tạo với dbHelper để chia sẻ kết nối
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

                    // SỬ DỤNG PHƯƠNG THỨC QUÁ TẢI, TRUYỀN DB HIỆN CÓ
                    Product product = productDAO_internal.getProductById(item.getProductId(), db);
                    if (product != null) {
                        double unitPrice;
                        // SỬ DỤNG PHƯƠNG THỨC QUÁ TẢI, TRUYỀN DB HIỆN CÓ
                        FlashSale flashSale = flashSaleDAO_internal.getFlashSaleByProductId(product.getId(), db);
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
                            db.endTransaction();
                            Log.e(TAG, "Transaction rolled back due to failed OrderDetail insert.");
                            return -1;
                        } else {
                            Log.d(TAG, "  -> OrderDetail for ProductID: " + item.getProductId() + " inserted successfully. Detail ID: " + detailId);
                        }

                        // 3. Cập nhật số lượng tồn kho của sản phẩm
                        int newStock = product.getStock() - item.getQuantity();
                        // SỬ DỤNG PHƯƠNG THỨC QUÁ TẢI, TRUYỀN DB HIỆN CÓ
                        int rowsAffected = productDAO_internal.updateProductStock(product.getId(), newStock, db);
                        if (rowsAffected <= 0) {
                            Log.e(TAG, "  -> FAILED to update stock for ProductID: " + product.getId() + ". Rows affected: " + rowsAffected);
                            db.endTransaction();
                            Log.e(TAG, "Transaction rolled back due to failed stock update.");
                            return -1;
                        } else {
                            Log.d(TAG, "  -> Stock updated for ProductID: " + product.getId() + ". New stock: " + newStock);
                        }

                    } else {
                        Log.e(TAG, "  -> Product with ID: " + item.getProductId() + " NOT FOUND in database while processing cart item.");
                        db.endTransaction();
                        Log.e(TAG, "Transaction rolled back because a product was not found.");
                        return -1;
                    }
                }
                db.setTransactionSuccessful();
                Log.d(TAG, "Transaction successful.");
            } else {
                Log.e(TAG, "Failed to create order header. orderId is -1.");
            }
        } catch (Exception e) {
            Log.e(TAG, "EXCEPTION during order creation: " + e.getMessage(), e);
            orderId = -1;
        } finally {
            if (db != null && db.inTransaction()) {
                db.endTransaction();
                Log.d(TAG, "Transaction ended.");
            }
            if (db != null && db.isOpen()) {
                db.close();
                Log.d(TAG, "Database connection closed.");
            }
        }
        Log.d(TAG, "createOrder finished. Returning orderId: " + orderId);
        return orderId;
    }

    // PHƯƠNG THỨC getOrdersByUserId() - ĐẢM BẢO NÓ NẰM NGOÀI PHƯƠNG THỨC createOrder()
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

    // PHƯƠNG THỨC getOrderDetailsByOrderId() - ĐẢM BẢO NÓ NẰM NGOÀI PHƯƠNG THỨC createOrder()
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

    // PHƯƠNG THỨC getOrderById() - ĐẢM BẢO NÓ NẰM NGOÀI PHƯƠNG THỨC createOrder()
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
}