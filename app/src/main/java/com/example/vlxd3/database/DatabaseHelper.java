// File: DatabaseHelper.java
package com.example.vlxd3.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class DatabaseHelper extends SQLiteOpenHelper {
    public static final String DATABASE_NAME = "vlxd3.db";
    public static final int DATABASE_VERSION = 5; // <-- TĂNG DATABASE_VERSION

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // SỬA BẢNG USERS ĐỂ THÊM CỘT EMAIL VÀ ADDRESS
        db.execSQL("CREATE TABLE users (id INTEGER PRIMARY KEY AUTOINCREMENT, username TEXT UNIQUE, password TEXT, fullName TEXT, phone TEXT, email TEXT, address TEXT)"); // <-- SỬA DÒNG NÀY
        db.execSQL("CREATE TABLE categories (id INTEGER PRIMARY KEY AUTOINCREMENT, name TEXT, image TEXT)");
        db.execSQL("CREATE TABLE products (id INTEGER PRIMARY KEY AUTOINCREMENT, name TEXT, categoryId INTEGER, price REAL, image TEXT, description TEXT, stock INTEGER, FOREIGN KEY(categoryId) REFERENCES categories(id))");
        db.execSQL("CREATE TABLE cart (id INTEGER PRIMARY KEY AUTOINCREMENT, userId INTEGER, productId INTEGER, quantity INTEGER, FOREIGN KEY(userId) REFERENCES users(id), FOREIGN KEY(productId) REFERENCES products(id))");
        db.execSQL("CREATE TABLE flash_sale (id INTEGER PRIMARY KEY AUTOINCREMENT, productId INTEGER, salePrice REAL, startDate TEXT, endDate TEXT, FOREIGN KEY(productId) REFERENCES products(id))");

        db.execSQL("CREATE TABLE orders (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "userId INTEGER, " +
                "orderDate TEXT, " +
                "totalAmount REAL, " +
                "customerName TEXT, " +
                "customerAddress TEXT, " +
                "customerPhone TEXT, " +
                "paymentMethod TEXT, " +
                "status TEXT, " +
                "FOREIGN KEY(userId) REFERENCES users(id))");

        db.execSQL("CREATE TABLE order_details (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "orderId INTEGER, " +
                "productId INTEGER, " +
                "quantity INTEGER, " +
                "unitPrice REAL, " +
                "FOREIGN KEY(orderId) REFERENCES orders(id), " +
                "FOREIGN KEY(productId) REFERENCES products(id))");


        // Thêm dữ liệu mẫu cho categories VỚI HÌNH ẢNH
        db.execSQL("INSERT INTO categories (name, image) VALUES " +
                "('Sắt', 'icon_cat_sat'), " +
                "('Cát', 'icon_cat_cat'), " +
                "('Gỗ', 'icon_cat_go'), " +
                "('Xi măng', 'icon_cat_ximang'), " +
                "('Thép', 'icon_cat_thep'), " +
                "('Gạch ốp lát', 'icon_cat_gachoplat')");

        // Thêm dữ liệu mẫu cho products
        db.execSQL("INSERT INTO products (name, categoryId, price, image, description, stock) VALUES " +
                "('Sắt phi 6', 1, 15000, '', 'Sắt phi 6 chất lượng cao', 100)," +
                "('Cát xây dựng', 2, 120000, '', 'Cát sạch dùng cho xây dựng', 50)," +
                "('Gỗ thông', 3, 250000, '', 'Gỗ thông nhập khẩu', 30)," +
                "('Xi măng Hà Tiên', 4, 95000, '', 'Xi măng Hà Tiên chất lượng tốt', 80)," +
                "('Thép cuộn', 5, 18000, '', 'Thép cuộn dùng cho xây dựng', 60)," +
                "('Gạch Lát Nền 60x60', 6, 120000, '', 'Gạch lát nền cao cấp 60x60 chống trơn trượt', 75)");

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-DD HH:mm:ss", Locale.getDefault());
        Calendar calendar = Calendar.getInstance();
        String startDate = sdf.format(calendar.getTime());

        calendar.add(Calendar.DAY_OF_YEAR, 7);
        String endDate = sdf.format(calendar.getTime());

        db.execSQL("INSERT INTO flash_sale (productId, salePrice, startDate, endDate) VALUES " +
                "(4, 76000.0, '" + startDate + "', '" + endDate + "')");
        db.execSQL("INSERT INTO flash_sale (productId, salePrice, startDate, endDate) VALUES " +
                "(6, 102000.0, '" + startDate + "', '" + endDate + "')");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS orders");
        db.execSQL("DROP TABLE IF EXISTS order_details");
        db.execSQL("DROP TABLE IF EXISTS flash_sale");
        db.execSQL("DROP TABLE IF EXISTS cart");
        db.execSQL("DROP TABLE IF EXISTS products");
        db.execSQL("DROP TABLE IF EXISTS categories");
        db.execSQL("DROP TABLE IF EXISTS users"); // Xóa bảng users cuối cùng
        onCreate(db);
    }
}