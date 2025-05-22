// File: FlashSaleDAO.java

package com.example.vlxd3.dao;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log; // Thêm import này

import com.example.vlxd3.database.DatabaseHelper;
import com.example.vlxd3.model.FlashSale;
import com.example.vlxd3.model.Product; // Có thể xóa nếu không dùng trực tiếp Product ở đây

import java.util.ArrayList;
import java.util.List;

public class FlashSaleDAO {
    private DatabaseHelper dbHelper;

    public FlashSaleDAO(Context context) {
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
            return id;
        } catch (Exception e) {
            Log.e("FlashSaleDAO", "Error adding flash sale: " + e.getMessage());
            return -1;
        } finally {
            if (db != null) db.close();
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
            Log.e("FlashSaleDAO", "Error getting all flash sales: " + e.getMessage());
        } finally {
            if (cursor != null) cursor.close();
            if (db != null) db.close();
        }
        return list;
    }

    public FlashSale getFlashSaleByProductId(int productId) {
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
        } catch (Exception e) {
            Log.e("FlashSaleDAO", "Error getting flash sale by product ID: " + e.getMessage());
        } finally {
            if (cursor != null) cursor.close();
            if (db != null) db.close();
        }
        return flashSale;
    }
}