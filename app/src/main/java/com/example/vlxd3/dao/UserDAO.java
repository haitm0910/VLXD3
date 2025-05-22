// File: UserDAO.java
package com.example.vlxd3.dao;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log; // Thêm import này

import com.example.vlxd3.database.DatabaseHelper;
import com.example.vlxd3.model.User;

public class UserDAO {
    private DatabaseHelper dbHelper;

    public UserDAO(Context context) {
        dbHelper = new DatabaseHelper(context);
    }

    public long registerUser(User user) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("username", user.getUsername());
        values.put("password", user.getPassword());
        values.put("fullName", user.getFullName());
        values.put("phone", user.getPhone());
        values.put("email", user.getEmail());   // <-- THÊM DÒNG NÀY
        values.put("address", user.getAddress()); // <-- THÊM DÒNG NÀY
        long id = db.insert("users", null, values);
        db.close();
        return id;
    }

    public User login(String username, String password) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.query("users", null, "username=? AND password=?", new String[]{username, password}, null, null, null);
        if (cursor != null && cursor.moveToFirst()) {
            User user = new User(
                    cursor.getInt(cursor.getColumnIndexOrThrow("id")),
                    cursor.getString(cursor.getColumnIndexOrThrow("username")),
                    cursor.getString(cursor.getColumnIndexOrThrow("password")),
                    cursor.getString(cursor.getColumnIndexOrThrow("fullName")),
                    cursor.getString(cursor.getColumnIndexOrThrow("phone")),
                    cursor.getString(cursor.getColumnIndexOrThrow("email")),   // <-- THÊM DÒNG NÀY
                    cursor.getString(cursor.getColumnIndexOrThrow("address")) // <-- THÊM DÒNG NÀY
            );
            cursor.close();
            db.close();
            return user;
        }
        if (cursor != null) cursor.close();
        db.close();
        return null;
    }

    public User getUserById(int id) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.query("users", null, "id=?", new String[]{String.valueOf(id)}, null, null, null);
        if (cursor != null && cursor.moveToFirst()) {
            User user = new User(
                    cursor.getInt(cursor.getColumnIndexOrThrow("id")),
                    cursor.getString(cursor.getColumnIndexOrThrow("username")),
                    cursor.getString(cursor.getColumnIndexOrThrow("password")),
                    cursor.getString(cursor.getColumnIndexOrThrow("fullName")),
                    cursor.getString(cursor.getColumnIndexOrThrow("phone")),
                    cursor.getString(cursor.getColumnIndexOrThrow("email")),   // <-- THÊM DÒNG NÀY
                    cursor.getString(cursor.getColumnIndexOrThrow("address")) // <-- THÊM DÒNG NÀY
            );
            cursor.close();
            db.close();
            return user;
        }
        if (cursor != null) cursor.close();
        db.close();
        return null;
    }

    // THÊM PHƯƠNG THỨC NÀY ĐỂ CẬP NHẬT THÔNG TIN NGƯỜI DÙNG
    public boolean updateUserInfo(User user) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("fullName", user.getFullName());
        values.put("phone", user.getPhone());
        values.put("email", user.getEmail());
        values.put("address", user.getAddress());

        // Chúng ta không cho phép thay đổi username và password qua màn hình này,
        // nếu muốn thay đổi, cần có chức năng riêng.
        int rowsAffected = db.update("users", values, "id=?", new String[]{String.valueOf(user.getId())});
        db.close();
        return rowsAffected > 0;
    }
}