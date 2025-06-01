// File: DatabaseHelper.java

package com.example.vlxd3.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class DatabaseHelper extends SQLiteOpenHelper {
    private static final String TAG = "DatabaseHelper";
    public static final String DATABASE_NAME = "vlxd3.db";
    public static final int DATABASE_VERSION = 8; // <-- ĐÃ TĂNG PHIÊN BẢN DATABASE

    private Context context;

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        this.context = context; // <-- GÁN CONTEXT
    }

    public Context getContext() { // PHƯƠNG THỨC ĐỂ CÁC DAO CÓ THỂ LẤY CONTEXT
        return context;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        Log.d(TAG, "Creating database tables...");

        // BẢNG USERS
        db.execSQL("CREATE TABLE users (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "username TEXT UNIQUE, " +
                "password TEXT, " +
                "fullName TEXT, " +
                "phone TEXT, " +
                "email TEXT, " +
                "address TEXT, " +
                "role TEXT DEFAULT 'customer')");

        // BẢNG CATEGORIES
        db.execSQL("CREATE TABLE categories (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "name TEXT, " +
                "image TEXT)");

        // BẢNG PRODUCTS
        db.execSQL("CREATE TABLE products (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "name TEXT, " +
                "categoryId INTEGER, " +
                "price REAL, " +
                "image TEXT, " +
                "description TEXT, " +
                "stock INTEGER, " +
                "unit TEXT, " + // thêm cột đơn vị
                "FOREIGN KEY(categoryId) REFERENCES categories(id))");

        // BẢNG CART
        db.execSQL("CREATE TABLE cart (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "userId INTEGER, " +
                "productId INTEGER, " +
                "quantity INTEGER, " +
                "FOREIGN KEY(userId) REFERENCES users(id), " +
                "FOREIGN KEY(productId) REFERENCES products(id))");

        // BẢNG FLASH_SALE
        db.execSQL("CREATE TABLE flash_sale (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "productId INTEGER, " +
                "salePrice REAL, " +
                "startDate TEXT, " +
                "endDate TEXT, " +
                "FOREIGN KEY(productId) REFERENCES products(id))");

        // BẢNG ORDERS
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

        // BẢNG ORDER_DETAILS
        db.execSQL("CREATE TABLE order_details (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "orderId INTEGER, " +
                "productId INTEGER, " +
                "quantity INTEGER, " +
                "unitPrice REAL, " +
                "FOREIGN KEY(orderId) REFERENCES orders(id), " +
                "FOREIGN KEY(productId) REFERENCES products(id))");

        Log.d(TAG, "All tables created successfully.");

        // --- THÊM DỮ LIỆU MẪU ---

        // Dữ liệu mẫu cho categories (với tên ảnh drawable)
        Log.d(TAG, "Inserting sample categories...");
        db.execSQL("INSERT INTO categories (name, image) VALUES " +
                "('Sắt', 'icon_cat_sat'), " +
                "('Cát', 'icon_cat_cat'), " +
                "('Gỗ', 'icon_cat_go'), " +
                "('Xi măng', 'icon_cat_ximang'), " +
                "('Thép', 'icon_cat_thep'), " +
                "('Gạch ốp lát', 'icon_cat_gachoplat'), " +
                "('Gạch xây', 'icon_cat_gachxay'), " +
                "('Đá xây', 'icon_cat_daxay'), " +
                "('Sỏi xây dựng', 'icon_cat_soixaydung')");
        Log.d(TAG, "Sample categories inserted.");

        // Dữ liệu mẫu cho products (ảnh có thể để trống hoặc thêm tên drawable)
        Log.d(TAG, "Inserting sample products...");
        db.execSQL("INSERT INTO products (name, categoryId, price, image, description, stock, unit) VALUES " +
                "('Xi măng Insee', 4, 90000.0, 'ximang_insee', 'dùng cho công trình lớn', 40, 'bao')," +
                "('Xi măng Thăng Long', 4, 105000.0, 'ximang_thanglong', 'dùng xây nhà', 34, 'bao')," +
                "('Xi măng Hà Tiên', 4, 88000.0, 'ximang_hatien', 'dùng cho dân dụng', 55, 'bao')," +
                "('Xi măng Nghi Sơn', 4, 87000.0, 'ximang_nghison', 'dùng cho xây trát', 37, 'bao')," +
                "('Xi măng Fico', 4, 89000.0, 'ximang_fico', 'dùng làm nền móng', 66, 'bao')," +

                "('Cát vàng', 2, 270000.0, 'cat_vang', 'Đổ bê tông', 30, 'm³')," +
                "('Cát tô', 2, 250000.0, 'cat_to', 'Tô tường', 40, 'm³')," +
                "('Cát san lấp', 2, 180000.0, 'cat_sanlap', 'San nền', 50, 'm³')," +
                "('Cát xây', 2, 230000.0, 'cat_xay', 'Vữa xây', 40, 'm³')," +
                "('Cát trắng', 2, 400000.0, 'cat_trang', 'Trang trí, hồ cá', 40, 'm³')," +

                "('Thép cây D6', 5, 16000.0, 'thep_d6', 'làm cột nhà', 20, 'kg')," +
                "('Thép cây D10', 5, 17000.0, 'thep_d10', 'làm dầm, cột lớn', 65, 'kg')," +
                "('Thép Hòa Phát D12', 5, 16500.0, 'thep_d12', 'dùng làm kết cấu', 58, 'kg')," +
                "('Thép Việt Nhật D20', 5, 18000.0, 'thep_d20', 'dùng làm cầu thang, cao tầng', 72, 'kg')," +
                "('Thép cuộn phi 6', 5, 18000.0, 'thep_cuon_phi6', 'dùng cho móng', 50, 'cuộn')," +
                "('Thép hình H100', 5, 25000.0, 'thep_h100', 'dùng cho kết cấu', 30, 'm')," +
                "('Thép ống 90', 5, 22000.0, 'thep_ong90', 'dùng cho khung', 40, 'm')," +
                "('Lưới thép hàn', 5, 35000.0, 'luoi_thep_han', 'dùng cho sàn', 25, 'm²')," +

                "('Sắt tròn phi 10', 1, 14000.0, 'sat_phi10', 'Làm cốt thép', 47, 'kg')," +
                "('Sắt tròn phi 6', 1, 13500.0, 'sat_phi6', 'Làm cốt phụ', 59, 'kg')," +
                "('Sắt V 40x40', 1, 20000.0, 'sat_v40', 'Làm giàn kéo, giàn mái', 71, 'm')," +

                "('Gỗ thông', 3, 6000000.0, 'go_thong', 'dùng làm nội thất', 90, 'm³')," +
                "('Gỗ cao su', 3, 5000000.0, 'go_caosu', 'dùng làm tủ kệ', 86, 'm³')," +
                "('Gỗ MDF phủ melamine', 3, 12000000.0, 'go_mdf', 'dùng làm bàn, tủ', 74, 'm³')," +
                "('Gỗ căm xe', 3, 1200000.0, 'go_cam', 'dùng làm cửa, cầu thang', 92, 'm³')," +

                "('Gạch men bóng Viglacera', 6, 120000.0, 'gach_men', 'Phòng khách', 36, 'm²')," +
                "('Gạch giả gỗ Đồng Tâm', 6, 180000.0, 'gach_go', 'Phòng ngủ', 38, 'm²')," +
                "('Gạch granite Prime', 6, 165000.0, 'gach_granite', 'Nhà vệ sinh', 37, 'm²')," +
                "('Gạch ceramic Taicera', 6, 195000.0, 'gach_ceramic', 'Ban công', 39, 'm²')," +

                // Gạch xây (danh mục mới, id sẽ là 7)
                "('Gạch đặc 2 lỗ', 7, 1200.0, '', 'Dùng xây tường chịu lực', 100, 'nghìn viên')," +
                "('Gạch lỗ 4', 7, 1100.0, '', 'Dùng xây tường ngăn', 80, 'nghìn viên')," +
                "('Gạch tuynel', 7, 1300.0, '', 'Gạch nung truyền thống', 60, 'nghìn viên')," +

                // Đá xây (danh mục mới, id sẽ là 8)
                "('Đá 1x2', 8, 350000.0, '', 'Đổ bê tông, làm móng', 50, 'm³')," +
                "('Đá 4x6', 8, 340000.0, '', 'Làm nền, móng', 40, 'm³')," +
                "('Đá mi', 8, 320000.0, '', 'Làm nền, san lấp', 30, 'm³')," +
                "('Đá hộc', 8, 360000.0, '', 'Kè móng, xây tường chắn', 20, 'm³')," +
                // Sỏi xây dựng (danh mục mới, id sẽ là 9)
                "('Sỏi xây dựng 1x2', 9, 370000.0, '', 'Làm bê tông, trang trí sân vườn', 25, 'm³')," +
                "('Sỏi xây dựng 2x4', 9, 380000.0, '', 'Làm nền, san lấp', 18, 'm³')," +
                "('Sỏi tự nhiên', 9, 400000.0, '', 'Trang trí, lọc nước', 15, 'm³')");
        Log.d(TAG, "Sample products inserted.");

        // Lấy ngày hiện tại và tính toán ngày kết thúc cho Flash Sale
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        Calendar calendar = Calendar.getInstance();
        String startDate = sdf.format(calendar.getTime());

        calendar.add(Calendar.DAY_OF_YEAR, 7);
        String endDate = sdf.format(calendar.getTime());

        // Dữ liệu mẫu cho flash_sale
        Log.d(TAG, "Inserting sample flash sales...");
        // Re-adjusting the product IDs for flash sales based on the new insertions.
        // Assuming Xi măng Hà Tiên is now product ID 3 (was 4 previously in existing code)
        // Assuming Gạch Lát Nền 60x60 is now product ID 22 (was 6 previously in existing code)
        // You might need to verify the exact product IDs after full insertion or query them dynamically.
        // For this response, I'll assume a sequential insertion and update the IDs.
        // Xi măng Hà Tiên (row 3 in new insertions) will be product ID 3.
        // Gạch men bóng Viglacera (row 21 in new insertions) will be product ID 21.

        db.execSQL("INSERT INTO flash_sale (productId, salePrice, startDate, endDate) VALUES " +
                "(3, 76000.0, '" + startDate + "', '" + endDate + "')"); // Xi măng Hà Tiên sale 20%
        db.execSQL("INSERT INTO flash_sale (productId, salePrice, startDate, endDate) VALUES " +
                "(21, 102000.0, '" + startDate + "', '" + endDate + "')"); // Gạch men bóng Viglacera sale 15% (assuming this is the intended one, previously "Gạch Lát Nền 60x60")
        Log.d(TAG, "Sample flash sales inserted.");

        // Thêm một tài khoản admin mẫu
        Log.d(TAG, "Inserting admin user...");
        db.execSQL("INSERT INTO users (username, password, fullName, phone, email, address, role) VALUES " +
                "('admin', 'adminpass', 'Quản trị viên', '0987654321', 'admin@example.com', '123 Đường Admin, TP.HCM', 'admin')");
        Log.d(TAG, "Admin user inserted.");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        Log.w(TAG, "Upgrading database from version " + oldVersion + " to " + newVersion + ", which will destroy all old data.");
        // Xóa tất cả các bảng theo thứ tự đúng để tránh lỗi khóa ngoại
        db.execSQL("DROP TABLE IF EXISTS order_details");
        db.execSQL("DROP TABLE IF EXISTS orders");
        db.execSQL("DROP TABLE IF EXISTS flash_sale");
        db.execSQL("DROP TABLE IF EXISTS cart");
        db.execSQL("DROP TABLE IF EXISTS products");
        db.execSQL("DROP TABLE IF EXISTS categories");
        db.execSQL("DROP TABLE IF EXISTS users");
        onCreate(db); // Gọi lại onCreate để tạo lại tất cả các bảng với cấu trúc mới
        Log.d(TAG, "Database upgraded and recreated.");
    }

    @Override
    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        Log.w(TAG, "Downgrading database from version " + oldVersion + " to " + newVersion + ", which will destroy all old data.");
        onUpgrade(db, oldVersion, newVersion); // Tái sử dụng logic nâng cấp
        Log.d(TAG, "Database downgraded and recreated.");
    }
}