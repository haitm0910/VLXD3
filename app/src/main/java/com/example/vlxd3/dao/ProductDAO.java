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
    private static final String TAG = "ProductDAO";
    private DatabaseHelper dbHelper;

    public ProductDAO(Context context) { // CHỈ GIỮ CONSTRUCTOR NÀY
        dbHelper = new DatabaseHelper(context);
    }

    // XÓA BỎ MỌI CONSTRUCTOR KHÁC (ví dụ: ProductDAO(DatabaseHelper dbHelper))

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
            if (db != null && db.isOpen()) db.close();
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
            if (db != null && db.isOpen()) db.close();
        }
        return list;
    }

    public Product getProductById(int id) { // PHƯƠNG THỨC NÀY SẼ TỰ MỞ VÀ ĐÓNG DB
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
            Log.e(TAG, "Error getting product by ID: " + e.getMessage(), e);
        } finally {
            if (cursor != null) cursor.close();
            if (db != null && db.isOpen()) db.close();
        }
        return product;
    }

    // XÓA BỎ MỌI PHƯƠNG THỨC getProductById QUÁ TẢI (ví dụ: Product getProductById(int id, SQLiteDatabase db))

    public int updateProductStock(int productId, int newStock) { // PHƯƠNG THỨC NÀY SẼ TỰ MỞ VÀ ĐÓNG DB
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
            if (db != null && db.isOpen()) db.close();
        }
    }

    // XÓA BỎ MỌI PHƯƠNG THỨC updateProductStock QUÁ TẢI (ví dụ: updateProductStock(int productId, int newStock, SQLiteDatabase db))

    public int deleteProductsByCategoryId(int categoryId, SQLiteDatabase db) {
        // Phương thức này vẫn sẽ nhận DB đã mở từ bên ngoài (CategoryDAO)
        // để đảm bảo transaction là đồng bộ.
        int rowsAffected = db.delete("products", "categoryId=?", new String[]{String.valueOf(categoryId)});
        Log.d(TAG, "Deleted " + rowsAffected + " products for category ID: " + categoryId);
        return rowsAffected;
        // KHÔNG ĐÓNG DB Ở ĐÂY, NGƯỜI GỌI (CategoryDAO) SẼ ĐÓNG.
    }

    public boolean deleteProduct(int productId) {
        SQLiteDatabase db = null;
        int rowsAffected = 0;
        try {
            db = dbHelper.getWritableDatabase();
            rowsAffected = db.delete("products", "id=?", new String[]{String.valueOf(productId)});
            Log.d(TAG, "Deleted product ID " + productId + ". Rows affected: " + rowsAffected);
        } catch (Exception e) {
            Log.e(TAG, "Error deleting product: " + e.getMessage(), e);
        } finally {
            if (db != null && db.isOpen()) db.close();
        }
        return rowsAffected > 0;
    }

    public boolean updateProduct(Product product) {
        SQLiteDatabase db = null;
        int rowsAffected = 0;
        try {
            db = dbHelper.getWritableDatabase();
            ContentValues values = new ContentValues();
            values.put("name", product.getName());
            values.put("categoryId", product.getCategoryId());
            values.put("price", product.getPrice());
            values.put("image", product.getImage());
            values.put("description", product.getDescription());
            values.put("stock", product.getStock());
            rowsAffected = db.update("products", values, "id=?", new String[]{String.valueOf(product.getId())});
            Log.d(TAG, "Updated product ID " + product.getId() + ". Rows affected: " + rowsAffected);
        } catch (Exception e) {
            Log.e(TAG, "Error updating product: " + e.getMessage(), e);
        } finally {
            if (db != null && db.isOpen()) db.close();
        }
        return rowsAffected > 0;
    }

    public List<Product> getAllProducts() {
        List<Product> list = new ArrayList<>();
        SQLiteDatabase db = null;
        Cursor cursor = null;
        try {
            db = dbHelper.getReadableDatabase();
            cursor = db.query("products", null, null, null, null, null, "name ASC");
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
            Log.e(TAG, "Error getting all products: " + e.getMessage(), e);
        } finally {
            if (cursor != null) cursor.close();
            if (db != null && db.isOpen()) db.close();
        }
        return list;
    }

    public List<Product> searchProductsByName(String query) {
        List<Product> list = new ArrayList<>();
        SQLiteDatabase db = null;
        Cursor cursor = null;
        try {
            db = dbHelper.getReadableDatabase();
            String selection = "name LIKE ?";
            String[] selectionArgs = new String[]{"%" + query + "%"};
            cursor = db.query("products", null, selection, selectionArgs, null, null, null);
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
            Log.d(TAG, "Search for '" + query + "' found " + list.size() + " products.");
        } catch (Exception e) {
            Log.e(TAG, "Error searching products by name: " + e.getMessage(), e);
        } finally {
            if (cursor != null) cursor.close();
            if (db != null && db.isOpen()) db.close();
        }
        return list;
    }
}