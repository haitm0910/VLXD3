package com.example.vlxd3.dao;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.example.vlxd3.database.DatabaseHelper;
import com.example.vlxd3.model.FlashSale;

import java.util.ArrayList;
import java.util.List;

public class FlashSaleDAO {
    private DatabaseHelper dbHelper;

    public FlashSaleDAO(Context context) {
        dbHelper = new DatabaseHelper(context);
    }

    public long addFlashSale(FlashSale flashSale) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("productId", flashSale.getProductId());
        values.put("salePrice", flashSale.getSalePrice());
        values.put("startDate", flashSale.getStartDate());
        values.put("endDate", flashSale.getEndDate());
        long id = db.insert("flash_sale", null, values);
        db.close();
        return id;
    }

    public List<FlashSale> getAllFlashSales() {
        List<FlashSale> list = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.query("flash_sale", null, null, null, null, null, null);
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
            cursor.close();
        }
        db.close();
        return list;
    }

    public FlashSale getFlashSaleByProductId(int productId) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.query("flash_sale", null, "productId=?", new String[]{String.valueOf(productId)}, null, null, null);
        if (cursor != null && cursor.moveToFirst()) {
            FlashSale flashSale = new FlashSale(
                cursor.getInt(cursor.getColumnIndexOrThrow("id")),
                cursor.getInt(cursor.getColumnIndexOrThrow("productId")),
                cursor.getDouble(cursor.getColumnIndexOrThrow("salePrice")),
                cursor.getString(cursor.getColumnIndexOrThrow("startDate")),
                cursor.getString(cursor.getColumnIndexOrThrow("endDate"))
            );
            cursor.close();
            db.close();
            return flashSale;
        }
        if (cursor != null) cursor.close();
        db.close();
        return null;
    }
}
