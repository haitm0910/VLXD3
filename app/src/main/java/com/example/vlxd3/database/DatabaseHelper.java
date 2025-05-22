// File: DatabaseHelper.java

package com.example.vlxd3.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.text.SimpleDateFormat; // Thêm import này
import java.util.Calendar; // Thêm import này
import java.util.Locale; // Thêm import này

public class DatabaseHelper extends SQLiteOpenHelper {
    public static final String DATABASE_NAME = "vlxd3.db";
    public static final int DATABASE_VERSION = 2;

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE users (id INTEGER PRIMARY KEY AUTOINCREMENT, username TEXT UNIQUE, password TEXT, fullName TEXT, phone TEXT)");
        db.execSQL("CREATE TABLE categories (id INTEGER PRIMARY KEY AUTOINCREMENT, name TEXT)");
        db.execSQL("CREATE TABLE products (id INTEGER PRIMARY KEY AUTOINCREMENT, name TEXT, categoryId INTEGER, price REAL, image TEXT, description TEXT, stock INTEGER, FOREIGN KEY(categoryId) REFERENCES categories(id))");
        db.execSQL("CREATE TABLE cart (id INTEGER PRIMARY KEY AUTOINCREMENT, userId INTEGER, productId INTEGER, quantity INTEGER, FOREIGN KEY(userId) REFERENCES users(id), FOREIGN KEY(productId) REFERENCES products(id))");
        db.execSQL("CREATE TABLE flash_sale (id INTEGER PRIMARY KEY AUTOINCREMENT, productId INTEGER, salePrice REAL, startDate TEXT, endDate TEXT, FOREIGN KEY(productId) REFERENCES products(id))");

        // Thêm dữ liệu mẫu cho categories
        db.execSQL("INSERT INTO categories (name) VALUES ('Sắt'), ('Cát'), ('Gỗ'), ('Xi măng'), ('Thép'), ('Gạch ốp lát')");

        // Thêm dữ liệu mẫu cho products
        db.execSQL("INSERT INTO products (name, categoryId, price, image, description, stock) VALUES " +
                "('Sắt phi 6', 1, 15000, '', 'Sắt phi 6 chất lượng cao', 100)," +
                "('Cát xây dựng', 2, 120000, '', 'Cát sạch dùng cho xây dựng', 50)," +
                "('Gỗ thông', 3, 250000, '', 'Gỗ thông nhập khẩu', 30)," +
                "('Xi măng Hà Tiên', 4, 95000, '', 'Xi măng Hà Tiên chất lượng tốt', 80)," +
                "('Thép cuộn', 5, 18000, '', 'Thép cuộn dùng cho xây dựng', 60)," +
                "('Gạch Lát Nền 60x60', 6, 120000, '', 'Gạch lát nền cao cấp 60x60 chống trơn trượt', 75)");

        // Lấy ngày hiện tại và tính toán ngày kết thúc cho Flash Sale
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        Calendar calendar = Calendar.getInstance();
        String startDate = sdf.format(calendar.getTime()); // Ngày bắt đầu là hiện tại

        calendar.add(Calendar.DAY_OF_YEAR, 7); // Flash Sale kéo dài 7 ngày
        String endDate = sdf.format(calendar.getTime()); // Ngày kết thúc sau 7 ngày

        // Thêm dữ liệu mẫu cho flash_sale
        // Sản phẩm 1: Xi măng Hà Tiên (id = 4), sale 20% từ 95.000 -> 76.000
        db.execSQL("INSERT INTO flash_sale (productId, salePrice, startDate, endDate) VALUES " +
                "(4, 76000.0, '" + startDate + "', '" + endDate + "')");

        // Sản phẩm 2: Gạch Lát Nền 60x60 (id = 6), sale 15% từ 120.000 -> 102.000
        db.execSQL("INSERT INTO flash_sale (productId, salePrice, startDate, endDate) VALUES " +
                "(6, 102000.0, '" + startDate + "', '" + endDate + "')");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Xóa tất cả các bảng và tạo lại
        db.execSQL("DROP TABLE IF EXISTS flash_sale");
        db.execSQL("DROP TABLE IF EXISTS cart");
        db.execSQL("DROP TABLE IF EXISTS products");
        db.execSQL("DROP TABLE IF EXISTS categories");
        db.execSQL("DROP TABLE IF EXISTS users");
        onCreate(db);
    }
}