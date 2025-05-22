// File: CategoryDAO.java

package com.example.vlxd3.dao;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log; // Thêm import này

import com.example.vlxd3.database.DatabaseHelper;
import com.example.vlxd3.model.Category;

import java.util.ArrayList;
import java.util.List;

public class CategoryDAO {
    private DatabaseHelper dbHelper;

    public CategoryDAO(Context context) {
        dbHelper = new DatabaseHelper(context);
    }

    public long addCategory(Category category) {
        SQLiteDatabase db = null;
        try {
            db = dbHelper.getWritableDatabase();
            ContentValues values = new ContentValues();
            values.put("name", category.getName());
            values.put("image", category.getImage());
            long id = db.insert("categories", null, values);
            return id;
        } catch (Exception e) {
            Log.e("CategoryDAO", "Error adding category: " + e.getMessage());
            return -1;
        } finally {
            if (db != null) db.close();
        }
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
            Log.e("CategoryDAO", "Error getting all categories: " + e.getMessage());
        } finally {
            if (cursor != null) cursor.close();
            if (db != null) db.close();
        }
        return list;
    }
}