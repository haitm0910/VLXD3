// File: UserDAO.java
package com.example.vlxd3.dao;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.example.vlxd3.database.DatabaseHelper;
import com.example.vlxd3.model.User;

public class UserDAO {
    private static final String TAG = "UserDAO";
    private DatabaseHelper dbHelper;

    public UserDAO(Context context) {
        dbHelper = new DatabaseHelper(context);
    }

    public long registerUser(User user) {
        SQLiteDatabase db = null;
        try {
            db = dbHelper.getWritableDatabase();
            ContentValues values = new ContentValues();
            values.put("username", user.getUsername());
            values.put("password", user.getPassword());
            values.put("fullName", user.getFullName());
            values.put("phone", user.getPhone());
            values.put("email", user.getEmail());
            values.put("address", user.getAddress());
            values.put("role", user.getRole()); // <-- THÊM DÒNG NÀY
            long id = db.insert("users", null, values);
            Log.d(TAG, "Registered user: " + user.getUsername() + " with ID: " + id + ", Role: " + user.getRole());
            return id;
        } catch (Exception e) {
            Log.e(TAG, "Error registering user: " + e.getMessage(), e);
            return -1;
        } finally {
            if (db != null) db.close();
        }
    }

    public User login(String username, String password) {
        SQLiteDatabase db = null;
        Cursor cursor = null;
        User user = null;
        try {
            db = dbHelper.getReadableDatabase();
            cursor = db.query("users", null, "username=? AND password=?", new String[]{username, password}, null, null, null);
            if (cursor != null && cursor.moveToFirst()) {
                user = new User(
                        cursor.getInt(cursor.getColumnIndexOrThrow("id")),
                        cursor.getString(cursor.getColumnIndexOrThrow("username")),
                        cursor.getString(cursor.getColumnIndexOrThrow("password")),
                        cursor.getString(cursor.getColumnIndexOrThrow("fullName")),
                        cursor.getString(cursor.getColumnIndexOrThrow("phone")),
                        cursor.getString(cursor.getColumnIndexOrThrow("email")),
                        cursor.getString(cursor.getColumnIndexOrThrow("address")),
                        cursor.getString(cursor.getColumnIndexOrThrow("role")) // <-- THÊM DÒNG NÀY
                );
            }
        } catch (Exception e) {
            Log.e(TAG, "Error logging in: " + e.getMessage(), e);
        } finally {
            if (cursor != null) cursor.close();
            if (db != null) db.close();
        }
        return user;
    }

    public User getUserById(int id) {
        SQLiteDatabase db = null;
        Cursor cursor = null;
        User user = null;
        try {
            db = dbHelper.getReadableDatabase();
            cursor = db.query("users", null, "id=?", new String[]{String.valueOf(id)}, null, null, null);
            if (cursor != null && cursor.moveToFirst()) {
                user = new User(
                        cursor.getInt(cursor.getColumnIndexOrThrow("id")),
                        cursor.getString(cursor.getColumnIndexOrThrow("username")),
                        cursor.getString(cursor.getColumnIndexOrThrow("password")),
                        cursor.getString(cursor.getColumnIndexOrThrow("fullName")),
                        cursor.getString(cursor.getColumnIndexOrThrow("phone")),
                        cursor.getString(cursor.getColumnIndexOrThrow("email")),
                        cursor.getString(cursor.getColumnIndexOrThrow("address")),
                        cursor.getString(cursor.getColumnIndexOrThrow("role")) // <-- THÊM DÒNG NÀY
                );
            }
        } catch (Exception e) {
            Log.e(TAG, "Error getting user by ID: " + e.getMessage(), e);
        } finally {
            if (cursor != null) cursor.close();
            if (db != null) db.close();
        }
        return user;
    }

    public boolean isUsernameExists(String username) {
        SQLiteDatabase db = null;
        Cursor cursor = null;
        boolean exists = false;
        try {
            db = dbHelper.getReadableDatabase();
            cursor = db.query("users", new String[]{"id"}, "username=?", new String[]{username}, null, null, null);
            if (cursor != null && cursor.getCount() > 0) {
                exists = true;
            }
        } catch (Exception e) {
            Log.e(TAG, "Error checking if username exists: " + e.getMessage(), e);
        } finally {
            if (cursor != null) cursor.close();
            if (db != null) db.close();
        }
        return exists;
    }

    public boolean updateUserInfo(User user) {
        SQLiteDatabase db = null;
        try {
            db = dbHelper.getWritableDatabase();
            ContentValues values = new ContentValues();
            values.put("fullName", user.getFullName());
            values.put("phone", user.getPhone());
            values.put("email", user.getEmail());
            values.put("address", user.getAddress());
            values.put("role", user.getRole()); // <-- THÊM DÒNG NÀY (nếu role có thể thay đổi qua đây)

            int rowsAffected = db.update("users", values, "id=?", new String[]{String.valueOf(user.getId())});
            Log.d(TAG, "Updated user info for ID " + user.getId() + ". Rows affected: " + rowsAffected);
            return rowsAffected > 0;
        } catch (Exception e) {
            Log.e(TAG, "Error updating user info: " + e.getMessage(), e);
            return false;
        } finally {
            if (db != null) db.close();
        }
    }

    public boolean updatePassword(String username, String newPassword) {
        SQLiteDatabase db = null;
        try {
            db = dbHelper.getWritableDatabase();
            ContentValues values = new ContentValues();
            values.put("password", newPassword);
            int rowsAffected = db.update("users", values, "username=?", new String[]{username});
            Log.d(TAG, "Updated password for user " + username + ". Rows affected: " + rowsAffected);
            return rowsAffected > 0;
        } catch (Exception e) {
            Log.e(TAG, "Error updating password: " + e.getMessage(), e);
            return false;
        } finally {
            if (db != null) db.close();
        }
    }
}