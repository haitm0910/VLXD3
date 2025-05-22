// File: ProductDAO.java

package com.example.vlxd3.dao;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log; // Thêm import này

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
        SQLiteDatabase db = null;
        try {
            db = dbHelper.getWritableDatabase();
            ContentValues values = new ContentValues();
            values.put("name", product.getName());
            values.put("categoryId", product.getCategoryId());
            values.put("price", product.getPrice());
            values.put("image", product.getImage());
            values.put("description", product.getDescription());
            values.put("stock", product.getStock());
            long id = db.insert("products", null, values);
            return id;
        } catch (Exception e) {
            Log.e("ProductDAO", "Error adding product: " + e.getMessage());
            return -1;
        } finally {
            if (db != null) db.close();
        }
    }

    public List<Product> getProductsByCategory(int categoryId) {
        List<Product> list = new ArrayList<>();
        SQLiteDatabase db = null;
        Cursor cursor = null;
        try {
            db = dbHelper.getReadableDatabase();
            cursor = db.query("products", null, "categoryId=?", new String[]{String.valueOf(categoryId)}, null, null, null);
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
            }
        } catch (Exception e) {
            Log.e("ProductDAO", "Error getting products by category: " + e.getMessage());
        } finally {
            if (cursor != null) cursor.close();
            if (db != null) db.close();
        }
        return list;
    }

    public Product getProductById(int id) {
        SQLiteDatabase db = null;
        Cursor cursor = null;
        Product product = null;
        try {
            db = dbHelper.getReadableDatabase();
            cursor = db.query("products", null, "id=?", new String[]{String.valueOf(id)}, null, null, null);
            if (cursor != null && cursor.moveToFirst()) {
                product = new Product(
                        cursor.getInt(cursor.getColumnIndexOrThrow("id")),
                        cursor.getString(cursor.getColumnIndexOrThrow("name")),
                        cursor.getInt(cursor.getColumnIndexOrThrow("categoryId")),
                        cursor.getDouble(cursor.getColumnIndexOrThrow("price")),
                        cursor.getString(cursor.getColumnIndexOrThrow("image")),
                        cursor.getString(cursor.getColumnIndexOrThrow("description")),
                        cursor.getInt(cursor.getColumnIndexOrThrow("stock"))
                );
            }
        } catch (Exception e) {
            Log.e("ProductDAO", "Error getting product by ID: " + e.getMessage());
        } finally {
            if (cursor != null) cursor.close();
            if (db != null) db.close();
        }
        return product;
    }

    public int updateProductStock(int productId, int newStock) {
        SQLiteDatabase db = null;
        try {
            db = dbHelper.getWritableDatabase();
            ContentValues values = new ContentValues();
            values.put("stock", newStock);
            int rowsAffected = db.update("products", values, "id=?", new String[]{String.valueOf(productId)});
            return rowsAffected;
        } catch (Exception e) {
            Log.e("ProductDAO", "Error updating product stock: " + e.getMessage());
            return -1;
        } finally {
            if (db != null) db.close();
        }
    }
}