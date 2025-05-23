// File: CategoryDAO.java

package com.example.vlxd3.dao;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.example.vlxd3.database.DatabaseHelper; // Đảm bảo import này
import com.example.vlxd3.model.Category;

import java.util.ArrayList;
import java.util.List;

public class CategoryDAO {
    private static final String TAG = "CategoryDAO";
    private DatabaseHelper dbHelper; // Biến này dùng để lấy kết nối DB

    public CategoryDAO(Context context) { // CHỈ GIỮ CONSTRUCTOR NÀY
        dbHelper = new DatabaseHelper(context);
    }

    // XÓA BỎ MỌI CONSTRUCTOR KHÁC (ví dụ: CategoryDAO(DatabaseHelper dbHelper))

    public long addCategory(Category category) {
        SQLiteDatabase db = null;
        try {
            db = dbHelper.getWritableDatabase();
            ContentValues values = new ContentValues();
            values.put("name", category.getName());
            values.put("image", category.getImage());
            long id = db.insert("categories", null, values);
            Log.d(TAG, "Added category: " + category.getName() + " with ID: " + id);
            return id;
        } catch (Exception e) {
            Log.e(TAG, "Error adding category: " + e.getMessage(), e);
            return -1;
        } finally {
            if (db != null && db.isOpen()) db.close(); // Đóng DB
        }
    }

    public boolean updateCategory(Category category) {
        SQLiteDatabase db = null;
        int rowsAffected = 0;
        try {
            db = dbHelper.getWritableDatabase();
            ContentValues values = new ContentValues();
            values.put("name", category.getName());
            values.put("image", category.getImage());
            rowsAffected = db.update("categories", values, "id=?", new String[]{String.valueOf(category.getId())});
            Log.d(TAG, "Updated category ID " + category.getId() + ". Rows affected: " + rowsAffected);
        } catch (Exception e) {
            Log.e(TAG, "Error updating category: " + e.getMessage(), e);
        } finally {
            if (db != null && db.isOpen()) db.close(); // Đóng DB
        }
        return rowsAffected > 0;
    }

    public boolean deleteCategory(int categoryId) {
        SQLiteDatabase db = null;
        int rowsAffected = 0;
        try {
            db = dbHelper.getWritableDatabase();
            db.beginTransaction(); // Bắt đầu transaction để xóa sản phẩm và danh mục

            // Xóa tất cả sản phẩm thuộc danh mục này trước để tránh lỗi khóa ngoại
            // ProductDAO sẽ được khởi tạo với Context, và tự quản lý DB của nó
            ProductDAO productDAO = new ProductDAO(dbHelper.getContext()); // LẤY CONTEXT TỪ DBHELPER HOẶC TRUYỀN TỪ ACITIVITY NẾU CÓ
            // Giả định dbHelper.getContext() có sẵn hoặc truyền context vào constructor CategoryDAO
            // Nếu không có, bạn cần truyền Context vào constructor CategoryDAO và lưu lại
            productDAO.deleteProductsByCategoryId(categoryId, db); // Phương thức này nhận DB đã mở

            rowsAffected = db.delete("categories", "id=?", new String[]{String.valueOf(categoryId)});
            Log.d(TAG, "Deleted category ID " + categoryId + ". Rows affected: " + rowsAffected);

            db.setTransactionSuccessful(); // Đánh dấu transaction thành công
        } catch (Exception e) {
            Log.e(TAG, "Error deleting category: " + e.getMessage(), e);
        } finally {
            if (db != null && db.inTransaction()) db.endTransaction(); // Kết thúc transaction
            if (db != null && db.isOpen()) db.close(); // Đóng DB
        }
        return rowsAffected > 0;
    }

    public List<Category> getAllCategories() {
        List<Category> list = new ArrayList<>();
        SQLiteDatabase db = null;
        Cursor cursor = null;
        try {
            db = dbHelper.getReadableDatabase();
            cursor = db.query("categories", null, null, null, null, null, null);
            if (cursor != null && cursor.moveToFirst()) {
                do {
                    Category category = new Category(
                            cursor.getInt(cursor.getColumnIndexOrThrow("id")),
                            cursor.getString(cursor.getColumnIndexOrThrow("name")),
                            cursor.getString(cursor.getColumnIndexOrThrow("image"))
                    );
                    list.add(category);
                } while (cursor.moveToNext());
            }
        } catch (Exception e) {
            Log.e(TAG, "Error getting all categories: " + e.getMessage(), e);
        } finally {
            if (cursor != null) cursor.close();
            if (db != null && db.isOpen()) db.close(); // Đóng DB
        }
        return list;
    }

    public Category getCategoryById(int id) {
        SQLiteDatabase db = null;
        Cursor cursor = null;
        Category category = null;
        try {
            db = dbHelper.getReadableDatabase();
            cursor = db.query("categories", null, "id=?", new String[]{String.valueOf(id)}, null, null, null);
            if (cursor != null && cursor.moveToFirst()) {
                category = new Category(
                        cursor.getInt(cursor.getColumnIndexOrThrow("id")),
                        cursor.getString(cursor.getColumnIndexOrThrow("name")),
                        cursor.getString(cursor.getColumnIndexOrThrow("image"))
                );
            }
        } catch (Exception e) {
            Log.e(TAG, "Error getting category by ID: " + e.getMessage(), e);
        } finally {
            if (cursor != null) cursor.close();
            if (db != null && db.isOpen()) db.close(); // Đóng DB
        }
        return category;
    }
}