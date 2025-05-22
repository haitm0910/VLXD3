// File: CartDAO.java

package com.example.vlxd3.dao;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.example.vlxd3.database.DatabaseHelper;
import com.example.vlxd3.model.CartItem;

// Các import liên quan đến file (BufferedReader, File, FileInputStream, etc.) có thể bị lỗi sau khi xóa addToCartToFile, bạn có thể xóa chúng đi nếu không còn sử dụng.
// import java.io.BufferedWriter; // <--- CÓ THỂ XÓA
// import java.io.File; // <--- CÓ THỂ XÓA
// import java.io.FileOutputStream; // <--- CÓ THỂ XÓA
// import java.io.IOException; // <--- CÓ THỂ XÓA
// import java.io.OutputStreamWriter; // <--- CÓ THỂ XÓA
import java.util.ArrayList;
import java.util.List;

public class CartDAO {
    private DatabaseHelper dbHelper;

    public CartDAO(Context context) {
        dbHelper = new DatabaseHelper(context);
    }

    public long addToCart(CartItem item) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        // Check if item exists
        Cursor cursor = db.query("cart", null, "userId=? AND productId=?", new String[]{String.valueOf(item.getUserId()), String.valueOf(item.getProductId())}, null, null, null);
        if (cursor != null && cursor.moveToFirst()) {
            // Update quantity
            int currentQty = cursor.getInt(cursor.getColumnIndexOrThrow("quantity"));
            ContentValues values = new ContentValues();
            values.put("quantity", currentQty + item.getQuantity());
            long result = db.update("cart", values, "userId=? AND productId=?", new String[]{String.valueOf(item.getUserId()), String.valueOf(item.getProductId())});
            cursor.close();
            db.close();
            return result;
        } else {
            // Insert new
            ContentValues values = new ContentValues();
            values.put("userId", item.getUserId());
            values.put("productId", item.getProductId());
            values.put("quantity", item.getQuantity());
            long id = db.insert("cart", null, values);
            if (cursor != null) cursor.close();
            db.close();
            return id;
        }
    }

    public List<CartItem> getCartItems(int userId) {
        List<CartItem> list = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.query("cart", null, "userId=?", new String[]{String.valueOf(userId)}, null, null, null);
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
            cursor.close();
        }
        db.close();
        return list;
    }

    public int updateQuantity(int userId, int productId, int quantity) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("quantity", quantity);
        int result = db.update("cart", values, "userId=? AND productId=?", new String[]{String.valueOf(userId), String.valueOf(productId)});
        db.close();
        return result;
    }

    public int removeFromCart(int userId, int productId) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        int result = db.delete("cart", "userId=? AND productId=?", new String[]{String.valueOf(userId), String.valueOf(productId)});
        db.close();
        return result;
    }

    public void clearCart(int userId) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        db.delete("cart", "userId=?", new String[]{String.valueOf(userId)});
        db.close();
    }

}