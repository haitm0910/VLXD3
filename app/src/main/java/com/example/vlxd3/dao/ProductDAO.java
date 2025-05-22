// File: ProductDAO.java

package com.example.vlxd3.dao;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.example.vlxd3.database.DatabaseHelper;
import com.example.vlxd3.model.Product;

import java.util.ArrayList;
import java.util.List;

public class ProductDAO {
    private static final String TAG = "ProductDAO"; // Tag cho Logcat
    private DatabaseHelper dbHelper;

    public ProductDAO(Context context) {
        dbHelper = new DatabaseHelper(context);
    }

    // Constructor mới nhận SQLiteDatabase (để sử dụng khi gọi từ DAO khác)
    // KHÔNG NÊN ĐÓNG DB Ở ĐÂY. DB SẼ ĐƯỢC ĐÓNG BỞI NGƯỜI GỌI.
    public ProductDAO(DatabaseHelper dbHelper) { // Thêm constructor này
        this.dbHelper = dbHelper;
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
            Log.d(TAG, "Added product: " + product.getName() + " with ID: " + id);
            return id;
        } catch (Exception e) {
            Log.e(TAG, "Error adding product: " + e.getMessage(), e);
            return -1;
        } finally {
            if (db != null && db.isOpen()) db.close(); // Chỉ đóng nếu được mở ở đây
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
            Log.e(TAG, "Error getting products by category: " + e.getMessage(), e);
        } finally {
            if (cursor != null) cursor.close();
            if (db != null && db.isOpen()) db.close(); // Chỉ đóng nếu được mở ở đây
        }
        return list;
    }

    // Phương thức getProductById ban đầu (mở và đóng DB riêng)
    public Product getProductById(int id) {
        SQLiteDatabase db = null;
        Cursor cursor = null;
        Product product = null;
        try {
            db = dbHelper.getReadableDatabase();
            Log.d(TAG, "getProductById: Database opened for reading (new connection).");
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
            Log.e(TAG, "Error getting product by ID (new connection): " + e.getMessage(), e);
        } finally {
            if (cursor != null) cursor.close();
            if (db != null && db.isOpen()) db.close(); // Chỉ đóng nếu được mở ở đây
            Log.d(TAG, "getProductById: Database connection closed (new connection).");
        }
        return product;
    }

    // PHƯƠNG THỨC getProductById quá tải: nhận một DB đã mở
    public Product getProductById(int id, SQLiteDatabase db) { // <-- THÊM PHƯƠNG THỨC NÀY
        Cursor cursor = null;
        Product product = null;
        try {
            // KHÔNG GỌI dbHelper.getReadableDatabase() ở đây
            Log.d(TAG, "getProductById (overload): Using existing database connection.");
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
            Log.e(TAG, "Error getting product by ID (using existing connection): " + e.getMessage(), e);
        } finally {
            if (cursor != null) cursor.close();
            // KHÔNG ĐÓNG DB Ở ĐÂY. DB ĐƯỢC QUẢN LÝ BỞI NGƯỜI GỌI.
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
            Log.d(TAG, "Updated stock for ProductID: " + productId + " to " + newStock + ". Rows affected: " + rowsAffected);
            return rowsAffected;
        } catch (Exception e) {
            Log.e(TAG, "Error updating product stock: " + e.getMessage(), e);
            return -1;
        } finally {
            if (db != null && db.isOpen()) db.close(); // Chỉ đóng nếu được mở ở đây
        }
    }

    // PHƯƠNG THỨC updateProductStock quá tải: nhận một DB đã mở
    public int updateProductStock(int productId, int newStock, SQLiteDatabase db) { // <-- THÊM PHƯƠNG THỨC NÀY
        ContentValues values = new ContentValues();
        values.put("stock", newStock);
        int rowsAffected = db.update("products", values, "id=?", new String[]{String.valueOf(productId)});
        Log.d(TAG, "Updated stock for ProductID: " + productId + " to " + newStock + " (using existing DB). Rows affected: " + rowsAffected);
        // KHÔNG ĐÓNG DB Ở ĐÂY
        return rowsAffected;
    }
}