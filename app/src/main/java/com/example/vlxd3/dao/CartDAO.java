// File: CartDAO.java

package com.example.vlxd3.dao;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log; // Thêm import này

import com.example.vlxd3.database.DatabaseHelper;
import com.example.vlxd3.model.CartItem;

import java.util.ArrayList;
import java.util.List;

public class CartDAO {
    private DatabaseHelper dbHelper;

    public CartDAO(Context context) {
        dbHelper = new DatabaseHelper(context);
    }

    public long addToCart(CartItem item) {
        SQLiteDatabase db = null;
        Cursor cursor = null;
        long result = -1;
        try {
            db = dbHelper.getWritableDatabase();
            // Check if item exists
            cursor = db.query("cart", null, "userId=? AND productId=?", new String[]{String.valueOf(item.getUserId()), String.valueOf(item.getProductId())}, null, null, null);
            if (cursor != null && cursor.moveToFirst()) {
                // Update quantity
                int currentQty = cursor.getInt(cursor.getColumnIndexOrThrow("quantity"));
                ContentValues values = new ContentValues();
                values.put("quantity", currentQty + item.getQuantity());
                result = db.update("cart", values, "userId=? AND productId=?", new String[]{String.valueOf(item.getUserId()), String.valueOf(item.getProductId())});
            } else {
                // Insert new
                ContentValues values = new ContentValues();
                values.put("userId", item.getUserId());
                values.put("productId", item.getProductId());
                values.put("quantity", item.getQuantity());
                result = db.insert("cart", null, values);
            }
        } catch (Exception e) {
            Log.e("CartDAO", "Error adding to cart: " + e.getMessage());
            result = -1;
        } finally {
            if (cursor != null) cursor.close();
            if (db != null) db.close();
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
        } catch (Exception e) {
            Log.e("CartDAO", "Error getting cart items: " + e.getMessage());
        } finally {
            if (cursor != null) cursor.close();
            if (db != null) db.close();
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
        } catch (Exception e) {
            Log.e("CartDAO", "Error updating quantity: " + e.getMessage());
        } finally {
            if (db != null) db.close();
        }
        return result;
    }

    public int removeFromCart(int userId, int productId) {
        SQLiteDatabase db = null;
        int result = -1;
        try {
            db = dbHelper.getWritableDatabase();
            result = db.delete("cart", "userId=? AND productId=?", new String[]{String.valueOf(userId), String.valueOf(productId)});
        } catch (Exception e) {
            Log.e("CartDAO", "Error removing from cart: " + e.getMessage());
        } finally {
            if (db != null) db.close();
        }
        return result;
    }

    public void clearCart(int userId) {
        SQLiteDatabase db = null;
        try {
            db = dbHelper.getWritableDatabase();
            db.delete("cart", "userId=?", new String[]{String.valueOf(userId)});
        } catch (Exception e) {
            Log.e("CartDAO", "Error clearing cart: " + e.getMessage());
        } finally {
            if (db != null) db.close();
        }
    }
}