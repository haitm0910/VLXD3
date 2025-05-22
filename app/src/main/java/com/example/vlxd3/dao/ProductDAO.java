package com.example.vlxd3.dao;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.example.vlxd3.database.DatabaseHelper;
import com.example.vlxd3.model.Product;

import java.util.ArrayList;
import java.util.List;

public class ProductDAO {
    private DatabaseHelper dbHelper;

    public ProductDAO(Context context) {
        dbHelper = new DatabaseHelper(context);
    }

    public long addProduct(Product product) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("name", product.getName());
        values.put("categoryId", product.getCategoryId());
        values.put("price", product.getPrice());
        values.put("image", product.getImage());
        values.put("description", product.getDescription());
        long id = db.insert("products", null, values);
        db.close();
        return id;
    }

    public List<Product> getProductsByCategory(int categoryId) {
        List<Product> list = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.query("products", null, "categoryId=?", new String[]{String.valueOf(categoryId)}, null, null, null);
        if (cursor != null && cursor.moveToFirst()) {
            do {
                Product product = new Product(
                    cursor.getInt(cursor.getColumnIndexOrThrow("id")),
                    cursor.getString(cursor.getColumnIndexOrThrow("name")),
                    cursor.getInt(cursor.getColumnIndexOrThrow("categoryId")),
                    cursor.getDouble(cursor.getColumnIndexOrThrow("price")),
                    cursor.getString(cursor.getColumnIndexOrThrow("image")),
                    cursor.getString(cursor.getColumnIndexOrThrow("description")),
                    cursor.getInt(cursor.getColumnIndexOrThrow("stock"))
                );
                list.add(product);
            } while (cursor.moveToNext());
            cursor.close();
        }
        db.close();
        return list;
    }

    public Product getProductById(int id) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.query("products", null, "id=?", new String[]{String.valueOf(id)}, null, null, null);
        if (cursor != null && cursor.moveToFirst()) {
            Product product = new Product(
                cursor.getInt(cursor.getColumnIndexOrThrow("id")),
                cursor.getString(cursor.getColumnIndexOrThrow("name")),
                cursor.getInt(cursor.getColumnIndexOrThrow("categoryId")),
                cursor.getDouble(cursor.getColumnIndexOrThrow("price")),
                cursor.getString(cursor.getColumnIndexOrThrow("image")),
                cursor.getString(cursor.getColumnIndexOrThrow("description")),
                cursor.getInt(cursor.getColumnIndexOrThrow("stock"))
            );
            cursor.close();
            db.close();
            return product;
        }
        if (cursor != null) cursor.close();
        db.close();
        return null;
    }
}
