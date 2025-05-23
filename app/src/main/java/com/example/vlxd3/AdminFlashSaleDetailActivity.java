// File: AdminFlashSaleDetailActivity.java
package com.example.vlxd3;

import android.app.DatePickerDialog; // Import này
import android.app.TimePickerDialog; // Import này
import android.os.Bundle;
import android.view.View; // Import View
import android.widget.AdapterView;
import android.widget.ArrayAdapter; // Import này
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner; // Import này
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.vlxd3.dao.FlashSaleDAO;
import com.example.vlxd3.dao.ProductDAO; // Import ProductDAO để lấy danh sách sản phẩm
import com.example.vlxd3.model.FlashSale;
import com.example.vlxd3.model.Product;

import java.text.ParseException; // Import này
import java.text.SimpleDateFormat;
import java.util.ArrayList; // Import này
import java.util.Calendar; // Import này
import java.util.List;
import java.util.Locale;

public class AdminFlashSaleDetailActivity extends AppCompatActivity {

    private TextView titleTextView;
    private Spinner productSpinner; // Spinner để chọn sản phẩm
    private EditText salePriceEt;
    private TextView startDateTv, startTimeTv, endDateTv, endTimeTv; // TextViews cho ngày/giờ
    private Button saveFlashSaleButton;
    private ImageView backButton;

    private FlashSaleDAO flashSaleDAO;
    private ProductDAO productDAO;
    private List<Product> allProducts; // Danh sách tất cả sản phẩm
    private int selectedProductId = -1; // ID của sản phẩm được chọn cho Flash Sale

    private int flashSaleId = -1; // -1 nếu là thêm mới, ID Flash Sale nếu là chỉnh sửa
    private boolean isAddingNew = true; // Cờ để phân biệt thêm mới hay chỉnh sửa

    private Calendar startCalendar, endCalendar; // Đối tượng Calendar để chọn ngày/giờ

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_flash_sale_detail); // Cần tạo layout này

        // Ánh xạ Views
        titleTextView = findViewById(R.id.tv_admin_flash_sale_detail_title);
        productSpinner = findViewById(R.id.spinner_select_product);
        salePriceEt = findViewById(R.id.et_admin_flash_sale_price);
        startDateTv = findViewById(R.id.tv_admin_flash_sale_start_date);
        startTimeTv = findViewById(R.id.tv_admin_flash_sale_start_time);
        endDateTv = findViewById(R.id.tv_admin_flash_sale_end_date);
        endTimeTv = findViewById(R.id.tv_admin_flash_sale_end_time);
        saveFlashSaleButton = findViewById(R.id.btn_admin_save_flash_sale);
        backButton = findViewById(R.id.backButtonAdminFlashSaleDetail);

        // Khởi tạo DAOs
        flashSaleDAO = new FlashSaleDAO(this);
        productDAO = new ProductDAO(this);

        // Lấy dữ liệu từ Intent
        flashSaleId = getIntent().getIntExtra("flashSaleId", -1);
        isAddingNew = getIntent().getBooleanExtra("isAddingNew", true);

        // Cập nhật tiêu đề Activity
        if (isAddingNew) {
            titleTextView.setText("Thêm Flash Sale mới");
            startCalendar = Calendar.getInstance(); // Khởi tạo với thời gian hiện tại
            endCalendar = Calendar.getInstance();
            endCalendar.add(Calendar.DAY_OF_MONTH, 7); // Mặc định kết thúc sau 7 ngày
        } else {
            titleTextView.setText("Chỉnh sửa Flash Sale");
            loadFlashSaleDetails(flashSaleId); // Tải thông tin Flash Sale nếu là chỉnh sửa
        }

        // Cấu hình Spinner chọn sản phẩm
        loadProductsIntoSpinner();

        // Xử lý chọn ngày/giờ
        startDateTv.setOnClickListener(v -> showDatePickerDialog(startDateTv, startCalendar));
        startTimeTv.setOnClickListener(v -> showTimePickerDialog(startTimeTv, startCalendar));
        endDateTv.setOnClickListener(v -> showDatePickerDialog(endDateTv, endCalendar));
        endTimeTv.setOnClickListener(v -> showTimePickerDialog(endTimeTv, endCalendar));

        // Cập nhật TextView ngày/giờ ban đầu
        updateDateTimeTextViews();

        // Xử lý nút back
        if (backButton != null) {
            backButton.setOnClickListener(v -> finish());
        }

        // Xử lý nút Lưu Flash Sale
        if (saveFlashSaleButton != null) {
            saveFlashSaleButton.setOnClickListener(v -> saveFlashSale());
        }
    }

    private void loadProductsIntoSpinner() {
        allProducts = productDAO.getAllProducts(); // Lấy tất cả sản phẩm
        List<String> productNames = new ArrayList<>();
        for (Product p : allProducts) {
            productNames.add(p.getName() + " (ID: " + p.getId() + ")");
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, productNames);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        productSpinner.setAdapter(adapter);

        productSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                selectedProductId = allProducts.get(position).getId(); // Lấy ID sản phẩm đã chọn
                // Cập nhật giá sale mặc định là giá gốc của sản phẩm đó
                salePriceEt.setText(String.valueOf(allProducts.get(position).getPrice()));
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                selectedProductId = -1;
                salePriceEt.setText("");
            }
        });
    }

    private void loadFlashSaleDetails(int id) {
        FlashSale flashSale = flashSaleDAO.getFlashSaleById(id);
        if (flashSale != null) {
            // Chọn sản phẩm trong Spinner
            for (int i = 0; i < allProducts.size(); i++) {
                if (allProducts.get(i).getId() == flashSale.getProductId()) {
                    productSpinner.setSelection(i);
                    selectedProductId = flashSale.getProductId(); // Đảm bảo ID được đặt
                    break;
                }
            }

            salePriceEt.setText(String.valueOf(flashSale.getSalePrice()));

            // Parse ngày/giờ và đặt vào Calendar
            SimpleDateFormat dateTimeFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
            try {
                startCalendar.setTime(dateTimeFormat.parse(flashSale.getStartDate()));
                endCalendar.setTime(dateTimeFormat.parse(flashSale.getEndDate()));
            } catch (ParseException e) {
                Toast.makeText(this, "Lỗi định dạng ngày giờ Flash Sale cũ.", Toast.LENGTH_SHORT).show();
            }
            updateDateTimeTextViews(); // Cập nhật TextViews
        } else {
            Toast.makeText(this, "Không tìm thấy Flash Sale để chỉnh sửa.", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    private void showDatePickerDialog(final TextView dateTextView, final Calendar calendar) {
        DatePickerDialog datePickerDialog = new DatePickerDialog(this,
                (view, year, month, dayOfMonth) -> {
                    calendar.set(Calendar.YEAR, year);
                    calendar.set(Calendar.MONTH, month);
                    calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                    updateDateTimeTextViews();
                }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH));
        datePickerDialog.show();
    }

    private void showTimePickerDialog(final TextView timeTextView, final Calendar calendar) {
        TimePickerDialog timePickerDialog = new TimePickerDialog(this,
                (view, hourOfDay, minute) -> {
                    calendar.set(Calendar.HOUR_OF_DAY, hourOfDay);
                    calendar.set(Calendar.MINUTE, minute);
                    updateDateTimeTextViews();
                }, calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE), true); // true for 24-hour format
        timePickerDialog.show();
    }

    private void updateDateTimeTextViews() {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());

        startDateTv.setText(dateFormat.format(startCalendar.getTime()));
        startTimeTv.setText(timeFormat.format(startCalendar.getTime()));
        endDateTv.setText(dateFormat.format(endCalendar.getTime()));
        endTimeTv.setText(timeFormat.format(endCalendar.getTime()));
    }

    private void saveFlashSale() {
        if (selectedProductId == -1) {
            Toast.makeText(this, "Vui lòng chọn một sản phẩm!", Toast.LENGTH_SHORT).show();
            return;
        }
        String salePriceStr = salePriceEt.getText().toString().trim();
        if (salePriceStr.isEmpty()) {
            Toast.makeText(this, "Vui lòng nhập giá Flash Sale!", Toast.LENGTH_SHORT).show();
            return;
        }
        double salePrice;
        try {
            salePrice = Double.parseDouble(salePriceStr);
        } catch (NumberFormatException e) {
            Toast.makeText(this, "Giá sale phải là số hợp lệ!", Toast.LENGTH_SHORT).show();
            return;
        }

        SimpleDateFormat dateTimeFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        String startDate = dateTimeFormat.format(startCalendar.getTime());
        String endDate = dateTimeFormat.format(endCalendar.getTime());

        // Kiểm tra ngày/giờ hợp lệ
        if (startCalendar.getTimeInMillis() >= endCalendar.getTimeInMillis()) {
            Toast.makeText(this, "Ngày/giờ kết thúc phải sau ngày/giờ bắt đầu!", Toast.LENGTH_LONG).show();
            return;
        }

        FlashSale flashSaleToSave;
        boolean success;

        if (isAddingNew) {
            flashSaleToSave = new FlashSale(selectedProductId, salePrice, startDate, endDate);
            long id = flashSaleDAO.addFlashSale(flashSaleToSave);
            success = (id != -1);
        } else {
            flashSaleToSave = new FlashSale(flashSaleId, selectedProductId, salePrice, startDate, endDate);
            success = flashSaleDAO.updateFlashSale(flashSaleToSave);
        }

        if (success) {
            Toast.makeText(this, (isAddingNew ? "Thêm" : "Cập nhật") + " Flash Sale thành công!", Toast.LENGTH_SHORT).show();
            finish(); // Đóng Activity và quay lại danh sách Flash Sale
        } else {
            Toast.makeText(this, (isAddingNew ? "Thêm" : "Cập nhật") + " Flash Sale thất bại!", Toast.LENGTH_SHORT).show();
        }
    }
}