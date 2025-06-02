// File: AdminProductDetailActivity.java
package com.example.vlxd3;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.vlxd3.dao.ProductDAO;
import com.example.vlxd3.model.Product;

public class AdminProductDetailActivity extends AppCompatActivity {

    private EditText productNameEt, productPriceEt, productStockEt, productDescriptionEt; // productImageEt cho tên file ảnh
    private ImageView productImagePreview; // ImageView để hiển thị ảnh
    private Button saveProductButton;
    private ImageView backButton;
    private TextView titleTextView;

    private ProductDAO productDAO;
    private int categoryId;
    private int productId = -1; // -1 nếu là thêm mới, ID sản phẩm nếu là chỉnh sửa
    private boolean isAddingNew = true; // Cờ để phân biệt thêm mới hay chỉnh sửa

    private static final int PICK_IMAGE_REQUEST = 1001;
    private Uri selectedImageUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_product_detail);

        // Ánh xạ Views
        productNameEt = findViewById(R.id.et_admin_product_name);
        productPriceEt = findViewById(R.id.et_admin_product_price);
        productStockEt = findViewById(R.id.et_admin_product_stock);
        productDescriptionEt = findViewById(R.id.et_admin_product_description);
        productImagePreview = findViewById(R.id.iv_admin_product_image_preview); // ImageView preview
        saveProductButton = findViewById(R.id.btn_admin_save_product);
        backButton = findViewById(R.id.backButtonAdminProductDetail);
        titleTextView = findViewById(R.id.tv_admin_product_detail_title);

        // Lấy dữ liệu từ Intent
        categoryId = getIntent().getIntExtra("categoryId", -1);
        productId = getIntent().getIntExtra("productId", -1);
        isAddingNew = getIntent().getBooleanExtra("isAddingNew", true);

        // Khởi tạo DAO
        productDAO = new ProductDAO(this);

        // Cập nhật tiêu đề Activity
        if (isAddingNew) {
            titleTextView.setText("Thêm sản phẩm mới");
        } else {
            titleTextView.setText("Chỉnh sửa sản phẩm");
            loadProductDetails(productId); // Tải thông tin sản phẩm nếu là chỉnh sửa
        }

        // Xử lý nút back
        if (backButton != null) {
            backButton.setOnClickListener(v -> finish());
        }

        // Xử lý nút Lưu sản phẩm
        if (saveProductButton != null) {
            saveProductButton.setOnClickListener(v -> saveProduct());
        }

        // Xử lý nút chọn ảnh từ album
        Button pickImageButton = findViewById(R.id.btn_pick_image);
        pickImageButton.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            intent.setType("image/*");
            startActivityForResult(intent, PICK_IMAGE_REQUEST);
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            selectedImageUri = data.getData();
            productImagePreview.setImageURI(selectedImageUri);
        }
    }

    private void loadProductDetails(int id) {
        Product product = productDAO.getProductById(id);
        if (product != null) {
            productNameEt.setText(product.getName());
            productPriceEt.setText(String.valueOf(product.getPrice()));
            productStockEt.setText(String.valueOf(product.getStock()));
            productDescriptionEt.setText(product.getDescription());
            // productImageEt.setText(product.getImage());
            // updateImagePreview(product.getImage()); // Cập nhật preview ảnh
            // Đảm bảo categoryId được đặt đúng nếu chuyển đổi danh mục
            categoryId = product.getCategoryId();
            // Khi load sản phẩm, nếu product.getImage() != null thì kiểm tra là URI hay tên drawable:
            if (product.getImage() != null && !product.getImage().isEmpty()) {
                if (product.getImage().startsWith("content://") || product.getImage().startsWith("file://")) {
                    selectedImageUri = Uri.parse(product.getImage());
                    productImagePreview.setImageURI(selectedImageUri);
                } else {
                    int resId = getResources().getIdentifier(product.getImage(), "drawable", getPackageName());
                    if (resId != 0) {
                        productImagePreview.setImageResource(resId);
                    } else {
                        productImagePreview.setImageResource(R.drawable.logo);
                    }
                }
            } else {
                productImagePreview.setImageResource(R.drawable.logo);
            }
            // TODO: Hiển thị đơn vị sản phẩm nếu có EditText hoặc Spinner cho đơn vị
        } else {
            Toast.makeText(this, "Không tìm thấy sản phẩm để chỉnh sửa.", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    private void saveProduct() {
        String name = productNameEt.getText().toString().trim();
        String priceStr = productPriceEt.getText().toString().trim();
        String stockStr = productStockEt.getText().toString().trim();
        String description = productDescriptionEt.getText().toString().trim();
        String image = (selectedImageUri != null) ? selectedImageUri.toString() : null; // Lưu URI ảnh vào DB
        String unit = ""; // TODO: Lấy đơn vị từ EditText hoặc Spinner nếu có

        if (name.isEmpty() || priceStr.isEmpty() || stockStr.isEmpty() || description.isEmpty()) {
            Toast.makeText(this, "Vui lòng nhập đầy đủ thông tin sản phẩm!", Toast.LENGTH_SHORT).show();
            return;
        }

        double price;
        int stock;
        try {
            price = Double.parseDouble(priceStr);
            stock = Integer.parseInt(stockStr);
        } catch (NumberFormatException e) {
            Toast.makeText(this, "Giá và số lượng phải là số hợp lệ!", Toast.LENGTH_SHORT).show();
            return;
        }

        Product productToSave;
        boolean success;

        if (isAddingNew) {
            productToSave = new Product(name, categoryId, price, image, description, stock, unit);
            long id = productDAO.addProduct(productToSave);
            success = (id != -1);
        } else {
            productToSave = new Product(productId, name, categoryId, price, image, description, stock, unit);
            success = productDAO.updateProduct(productToSave);
        }

        if (success) {
            Toast.makeText(this, (isAddingNew ? "Thêm" : "Cập nhật") + " sản phẩm thành công!", Toast.LENGTH_SHORT).show();
            finish(); // Đóng Activity và quay lại danh sách sản phẩm
        } else {
            Toast.makeText(this, (isAddingNew ? "Thêm" : "Cập nhật") + " sản phẩm thất bại!", Toast.LENGTH_SHORT).show();
        }
    }
}