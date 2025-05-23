// File: CartDAO.java

package com.example.vlxd3.dao;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.example.vlxd3.database.DatabaseHelper;
import com.example.vlxd3.model.CartItem;

import java.util.ArrayList;
import java.util.List;

public class CartDAO {
    private static final String TAG = "CartDAO";
    private DatabaseHelper dbHelper;

    public CartDAO(Context context) { // Constructor duy nhất
        dbHelper = new DatabaseHelper(context);
    }

    public long addToCart(CartItem item) {
        SQLiteDatabase db = null;
        Cursor cursor = null;
        long result = -1;
        try {
            db = dbHelper.getWritableDatabase();
            // Kiểm tra xem item đã tồn tại trong giỏ hàng của user đó chưa
            cursor = db.query("cart", null, "userId=? AND productId=?", new String[]{String.valueOf(item.getUserId()), String.valueOf(item.getProductId())}, null, null, null);
            if (cursor != null && cursor.moveToFirst()) {
                // Nếu tồn tại, cập nhật số lượng
                int currentQty = cursor.getInt(cursor.getColumnIndexOrThrow("quantity"));
                ContentValues values = new ContentValues();
                values.put("quantity", currentQty + item.getQuantity());
                result = db.update("cart", values, "userId=? AND productId=?", new String[]{String.valueOf(item.getUserId()), String.valueOf(item.getProductId())});
                Log.d(TAG, "Updated cart item: userId=" + item.getUserId() + ", productId=" + item.getProductId() + ", new quantity=" + (currentQty + item.getQuantity()));
            } else {
                // Nếu chưa tồn tại, thêm mới
                ContentValues values = new ContentValues();
                values.put("userId", item.getUserId());
                values.put("productId", item.getProductId());
                values.put("quantity", item.getQuantity());
                result = db.insert("cart", null, values);
                Log.d(TAG, "Added new cart item: userId=" + item.getUserId() + ", productId=" + item.getProductId() + ", quantity=" + item.getQuantity());
            }
        } catch (Exception e) {
            Log.e(TAG, "Error adding to cart: " + e.getMessage(), e);
            result = -1;
        } finally {
            if (cursor != null) cursor.close();
            if (db != null && db.isOpen()) db.close();
        }
        return result;
    }

    public List<CartItem> getCartItems(int userId) {
        List<CartItem> list = new ArrayList<>();
        SQLiteDatabase db = null;
        Cursor cursor = null;
        try {
            db = dbHelper.getReadableDatabase();
            cursor = db.query("cart", null, "userId=?", new String[]{String.valueOf(userId)}, null, null, null);
            if (cursor != null && cursor.moveToFirst()) {
                do {
                    CartItem item = new CartItem(
                            cursor.getInt(cursor.getColumnIndexOrThrow("id")),
                            cursor.getInt(cursor.getColumnIndexOrThrow("userId")),
                            cursor.getInt(cursor.getColumnIndexOrThrow("productId")),
                            cursor.getInt(cursor.getColumnIndexOrThrow("quantity"))
                    );
                    list.add(item);
                } while (cursor.moveToNext());
            }
            Log.d(TAG, "Retrieved " + list.size() + " cart items for userId: " + userId);
        } catch (Exception e) {
            Log.e(TAG, "Error getting cart items: " + e.getMessage(), e);
        } finally {
            if (cursor != null) cursor.close();
            if (db != null && db.isOpen()) db.close();
        }
        return list;
    }

    public int updateQuantity(int userId, int productId, int quantity) {
        SQLiteDatabase db = null;
        int result = -1;
        try {
            db = dbHelper.getWritableDatabase();
            ContentValues values = new ContentValues();
            values.put("quantity", quantity);
            result = db.update("cart", values, "userId=? AND productId=?", new String[]{String.valueOf(userId), String.valueOf(productId)});
            Log.d(TAG, "Updated quantity for userId=" + userId + ", productId=" + productId + " to " + quantity + ". Rows affected: " + result);
        } catch (Exception e) {
            Log.e(TAG, "Error updating quantity: " + e.getMessage(), e);
        } finally {
            if (db != null && db.isOpen()) db.close();
        }
        return result;
    }

    public int removeFromCart(int userId, int productId) {
        SQLiteDatabase db = null;
        int result = -1;
        try {
            db = dbHelper.getWritableDatabase();
            result = db.delete("cart", "userId=? AND productId=?", new String[]{String.valueOf(userId), String.valueOf(productId)});
            Log.d(TAG, "Removed cart item: userId=" + userId + ", productId=" + productId + ". Rows affected: " + result);
        } catch (Exception e) {
            Log.e(TAG, "Error removing from cart: " + e.getMessage(), e);
        } finally {
            if (db != null && db.isOpen()) db.close();
        }
        return result;
    }

    public void clearCart(int userId) {
        SQLiteDatabase db = null;
        try {
            db = dbHelper.getWritableDatabase();
            int rowsDeleted = db.delete("cart", "userId=?", new String[]{String.valueOf(userId)});
            Log.d(TAG, "Cleared cart for userId: " + userId + ". Rows deleted: " + rowsDeleted);
        } catch (Exception e) {
            Log.e(TAG, "Error clearing cart: " + e.getMessage(), e);
        } finally {
            if (db != null && db.isOpen()) db.close();
        }
    }
}