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

    public FlashSaleDAO(Context context) { // Constructor duy nhất
        this.dbHelper = new DatabaseHelper(context);
    }

    public FlashSaleDAO(DatabaseHelper dbHelper) { // Constructor quá tải cho OrderDAO
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
            cursor = db.query("flash_sale", null, null, null, null, null, "startDate DESC");
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
            Log.d(TAG, "Retrieved " + list.size() + " flash sales.");
        } catch (Exception e) {
            Log.e(TAG, "Error getting all flash sales: " + e.getMessage(), e);
        } finally {
            if (cursor != null) cursor.close();
            if (db != null && db.isOpen()) db.close();
        }
        return list;
    }

    public FlashSale getFlashSaleById(int id) {
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
            Log.d(TAG, "Retrieved flash sale by ID " + id + ": " + (flashSale != null ? flashSale.getProductId() : "null"));
        } catch (Exception e) {
            Log.e(TAG, "Error getting flash sale by ID: " + e.getMessage(), e);
        } finally {
            if (cursor != null) cursor.close();
            if (db != null && db.isOpen()) db.close();
        }
        return flashSale;
    }

    public FlashSale getFlashSaleByProductId(int productId) { // Phương thức này tự mở và đóng DB
        SQLiteDatabase db = null;
        Cursor cursor = null;
        FlashSale flashSale = null;
        try {
            db = dbHelper.getReadableDatabase();
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
            Log.d(TAG, "Retrieved flash sale by Product ID " + productId + ": " + (flashSale != null ? flashSale.getSalePrice() : "null"));
        } catch (Exception e) {
            Log.e(TAG, "Error getting flash sale by product ID: " + e.getMessage(), e);
        } finally {
            if (cursor != null) cursor.close();
            if (db != null && db.isOpen()) db.close();
        }
        return flashSale;
    }

    public FlashSale getFlashSaleByProductId(int productId, SQLiteDatabase db) { // Phương thức quá tải (Dùng khi gọi từ DAO khác trong transaction)
        Cursor cursor = null;
        FlashSale flashSale = null;
        try {
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
            Log.d(TAG, "Retrieved flash sale by Product ID " + productId + " (using existing DB): " + (flashSale != null ? flashSale.getSalePrice() : "null"));
        } catch (Exception e) {
            Log.e(TAG, "Error getting flash sale by product ID (using existing DB): " + e.getMessage(), e);
        } finally {
            if (cursor != null) cursor.close();
            // KHÔNG ĐÓNG DB Ở ĐÂY. DB ĐƯỢC QUẢN LÝ BỞI NGƯỜI GỌI.
        }
        return flashSale;
    }
}