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
            long id = db.insert("users", null, values);
            return id;
        } catch (Exception e) {
            Log.e("UserDAO", "Error registering user: " + e.getMessage());
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
                        cursor.getString(cursor.getColumnIndexOrThrow("address"))
                );
            }
        } catch (Exception e) {
            Log.e("UserDAO", "Error logging in: " + e.getMessage());
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
                        cursor.getString(cursor.getColumnIndexOrThrow("address"))
                );
            }
        } catch (Exception e) {
            Log.e("UserDAO", "Error getting user by ID: " + e.getMessage());
        } finally {
            if (cursor != null) cursor.close();
            if (db != null) db.close();
        }
        return user;
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

            int rowsAffected = db.update("users", values, "id=?", new String[]{String.valueOf(user.getId())});
            return rowsAffected > 0;
        } catch (Exception e) {
            Log.e("UserDAO", "Error updating user info: " + e.getMessage());
            return false;
        } finally {
            if (db != null) db.close();
        }
    }
}