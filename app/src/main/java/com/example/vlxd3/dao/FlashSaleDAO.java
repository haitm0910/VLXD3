// File: FlashSaleDAO.java

package com.example.vlxd3.dao;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.example.vlxd3.database.DatabaseHelper;
import com.example.vlxd3.model.FlashSale;
import com.example.vlxd3.model.Product;

import java.util.ArrayList;
import java.util.List;

public class FlashSaleDAO {
    private static final String TAG = "FlashSaleDAO"; // Tag cho Logcat
    private DatabaseHelper dbHelper;

    public FlashSaleDAO(Context context) {
        dbHelper = new DatabaseHelper(context);
    }

    // Constructor mới nhận SQLiteDatabase (để sử dụng khi gọi từ DAO khác)
    // KHÔNG NÊN ĐÓNG DB Ở ĐÂY. DB SẼ ĐƯỢC ĐÓNG BỞI NGƯỜI GỌI.
    public FlashSaleDAO(DatabaseHelper dbHelper) { // Thêm constructor này
        this.dbHelper = dbHelper;
    }

    public long addFlashSale(FlashSale flashSale) {
        SQLiteDatabase db = null;
        try {
            db = dbHelper.getWritableDatabase();
            ContentValues values = new ContentValues();
            values.put("productId", flashSale.getProductId());
            values.put("salePrice", flashSale.getSalePrice());
            values.put("startDate", flashSale.getStartDate());
            values.put("endDate", flashSale.getEndDate());
            long id = db.insert("flash_sale", null, values);
            Log.d(TAG, "Added flash sale for product: " + flashSale.getProductId() + " with ID: " + id);
            return id;
        } catch (Exception e) {
            Log.e(TAG, "Error adding flash sale: " + e.getMessage(), e);
            return -1;
        } finally {
            if (db != null && db.isOpen()) db.close();
        }
    }

    public List<FlashSale> getAllFlashSales() {
        List<FlashSale> list = new ArrayList<>();
        SQLiteDatabase db = null;
        Cursor cursor = null;
        try {
            db = dbHelper.getReadableDatabase();
            cursor = db.query("flash_sale", null, null, null, null, null, null);
            if (cursor != null && cursor.moveToFirst()) {
                do {
                    FlashSale flashSale = new FlashSale(
                            cursor.getInt(cursor.getColumnIndexOrThrow("id")),
                            cursor.getInt(cursor.getColumnIndexOrThrow("productId")),
                            cursor.getDouble(cursor.getColumnIndexOrThrow("salePrice")),
                            cursor.getString(cursor.getColumnIndexOrThrow("startDate")),
                            cursor.getString(cursor.getColumnIndexOrThrow("endDate"))
                    );
                    list.add(flashSale);
                } while (cursor.moveToNext());
            }
        } catch (Exception e) {
            Log.e(TAG, "Error getting all flash sales: " + e.getMessage(), e);
        } finally {
            if (cursor != null) cursor.close();
            if (db != null && db.isOpen()) db.close();
        }
        return list;
    }

    // Phương thức getFlashSaleByProductId ban đầu
    public FlashSale getFlashSaleByProductId(int productId) {
        SQLiteDatabase db = null;
        Cursor cursor = null;
        FlashSale flashSale = null;
        try {
            db = dbHelper.getReadableDatabase();
            Log.d(TAG, "getFlashSaleByProductId: Database opened for reading (new connection).");
            cursor = db.query("flash_sale", null, "productId=?", new String[]{String.valueOf(productId)}, null, null, null);
            if (cursor != null && cursor.moveToFirst()) {
                flashSale = new FlashSale(
                        cursor.getInt(cursor.getColumnIndexOrThrow("id")),
                        cursor.getInt(cursor.getColumnIndexOrThrow("productId")),
                        cursor.getDouble(cursor.getColumnIndexOrThrow("salePrice")),
                        cursor.getString(cursor.getColumnIndexOrThrow("startDate")),
                        cursor.getString(cursor.getColumnIndexOrThrow("endDate"))
                );
            }
        } catch (Exception e) {
            Log.e(TAG, "Error getting flash sale by product ID (new connection): " + e.getMessage(), e);
        } finally {
            if (cursor != null) cursor.close();
            if (db != null && db.isOpen()) db.close();
            Log.d(TAG, "getFlashSaleByProductId: Database connection closed (new connection).");
        }
        return flashSale;
    }

    // PHƯƠNG THỨC getFlashSaleByProductId quá tải: nhận một DB đã mở
    public FlashSale getFlashSaleByProductId(int productId, SQLiteDatabase db) { // <-- THÊM PHƯƠNG THỨC NÀY
        Cursor cursor = null;
        FlashSale flashSale = null;
        try {
            // KHÔNG GỌI dbHelper.getReadableDatabase() ở đây
            Log.d(TAG, "getFlashSaleByProductId (overload): Using existing database connection.");
            cursor = db.query("flash_sale", null, "productId=?", new String[]{String.valueOf(productId)}, null, null, null);
            if (cursor != null && cursor.moveToFirst()) {
                flashSale = new FlashSale(
                        cursor.getInt(cursor.getColumnIndexOrThrow("id")),
                        cursor.getInt(cursor.getColumnIndexOrThrow("productId")),
                        cursor.getDouble(cursor.getColumnIndexOrThrow("salePrice")),
                        cursor.getString(cursor.getColumnIndexOrThrow("startDate")),
                        cursor.getString(cursor.getColumnIndexOrThrow("endDate"))
                );
            }
        } catch (Exception e) {
            Log.e(TAG, "Error getting flash sale by product ID (using existing connection): " + e.getMessage(), e);
        } finally {
            if (cursor != null) cursor.close();
            // KHÔNG ĐÓNG DB Ở ĐÂY. DB ĐƯỢC QUẢN LÝ BỞI NGƯỜI GỌI.
        }
        return flashSale;
    }
}