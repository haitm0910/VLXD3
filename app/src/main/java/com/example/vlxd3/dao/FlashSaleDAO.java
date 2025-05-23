// File: FlashSaleDAO.java

package com.example.vlxd3.dao;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.example.vlxd3.database.DatabaseHelper;
import com.example.vlxd3.model.FlashSale;

import java.util.ArrayList;
import java.util.List;

public class FlashSaleDAO {
    private static final String TAG = "FlashSaleDAO";
    private DatabaseHelper dbHelper;

    public FlashSaleDAO(Context context) { // Constructor chính
        dbHelper = new DatabaseHelper(context);
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

    // PHƯƠNG THỨC MỚI: Cập nhật Flash Sale
    public boolean updateFlashSale(FlashSale flashSale) {
        SQLiteDatabase db = null;
        int rowsAffected = 0;
        try {
            db = dbHelper.getWritableDatabase();
            ContentValues values = new ContentValues();
            values.put("productId", flashSale.getProductId());
            values.put("salePrice", flashSale.getSalePrice());
            values.put("startDate", flashSale.getStartDate());
            values.put("endDate", flashSale.getEndDate());
            rowsAffected = db.update("flash_sale", values, "id=?", new String[]{String.valueOf(flashSale.getId())});
            Log.d(TAG, "Updated flash sale ID " + flashSale.getId() + ". Rows affected: " + rowsAffected);
        } catch (Exception e) {
            Log.e(TAG, "Error updating flash sale: " + e.getMessage(), e);
        } finally {
            if (db != null && db.isOpen()) db.close();
        }
        return rowsAffected > 0;
    }

    // PHƯƠNG THỨC MỚI: Xóa Flash Sale
    public boolean deleteFlashSale(int flashSaleId) {
        SQLiteDatabase db = null;
        int rowsAffected = 0;
        try {
            db = dbHelper.getWritableDatabase();
            rowsAffected = db.delete("flash_sale", "id=?", new String[]{String.valueOf(flashSaleId)});
            Log.d(TAG, "Deleted flash sale ID " + flashSaleId + ". Rows affected: " + rowsAffected);
        } catch (Exception e) {
            Log.e(TAG, "Error deleting flash sale: " + e.getMessage(), e);
        } finally {
            if (db != null && db.isOpen()) db.close();
        }
        return rowsAffected > 0;
    }

    public List<FlashSale> getAllFlashSales() {
        List<FlashSale> list = new ArrayList<>();
        SQLiteDatabase db = null;
        Cursor cursor = null;
        try {
            db = dbHelper.getReadableDatabase();
            cursor = db.query("flash_sale", null, null, null, null, null, "startDate DESC"); // Sắp xếp theo ngày bắt đầu
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

    public FlashSale getFlashSaleById(int id) { // Phương thức mới để lấy FlashSale theo ID
        SQLiteDatabase db = null;
        Cursor cursor = null;
        FlashSale flashSale = null;
        try {
            db = dbHelper.getReadableDatabase();
            cursor = db.query("flash_sale", null, "id=?", new String[]{String.valueOf(id)}, null, null, null);
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
            Log.e(TAG, "Error getting flash sale by ID: " + e.getMessage(), e);
        } finally {
            if (cursor != null) cursor.close();
            if (db != null && db.isOpen()) db.close();
        }
        return flashSale;
    }

    public FlashSale getFlashSaleByProductId(int productId) { // Phương thức đã có
        SQLiteDatabase db = null;
        Cursor cursor = null;
        FlashSale flashSale = null;
        try {
            db = dbHelper.getReadableDatabase();
            // Có thể thêm điều kiện ngày để chỉ lấy flash sale đang hoạt động
            // Ví dụ: "productId=? AND startDate <= DATE('now') AND endDate >= DATE('now')"
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
            Log.e(TAG, "Error getting flash sale by product ID: " + e.getMessage(), e);
        } finally {
            if (cursor != null) cursor.close();
            if (db != null && db.isOpen()) db.close();
        }
        return flashSale;
    }
}