// File: PersonalActivity.java (tên file của bạn)

package com.example.vlxd3;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.vlxd3.dao.UserDAO;
import com.example.vlxd3.model.User;

public class PersonalActivity extends AppCompatActivity { // <-- Đã sửa tên lớp ở đây

    private EditText fullNameEditText, emailEditText, phoneNumberEditText, addressEditText;
    private ImageView personalAvatarImageView;
    private Button saveInfoButton;
    private ImageView backButtonPersonal; // Ánh xạ nút back

    private int userId;
    private UserDAO userDAO;
    private User currentUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_personal); // Đảm bảo sử dụng đúng layout activity_personal.xml

        // Ánh xạ Views từ layout mới
        fullNameEditText = findViewById(R.id.et_full_name);
        emailEditText = findViewById(R.id.et_email);
        phoneNumberEditText = findViewById(R.id.et_phone_number);
        addressEditText = findViewById(R.id.et_address);
        personalAvatarImageView = findViewById(R.id.iv_personal_avatar);
        saveInfoButton = findViewById(R.id.btn_save_personal_info);
        backButtonPersonal = findViewById(R.id.iv_back_arrow_personal_info); // Ánh xạ nút back mới


        // Khởi tạo DAO
        userDAO = new UserDAO(this);

        // Lấy userId từ Intent
        userId = getIntent().getIntExtra("userId", -1);

        if (userId == -1) {
            Toast.makeText(this, "Không tìm thấy thông tin người dùng. Vui lòng đăng nhập lại.", Toast.LENGTH_LONG).show();
            Intent loginIntent = new Intent(this, LoginActivity.class);
            startActivity(loginIntent);
            finish();
            return;
        }

        // Tải thông tin người dùng hiện tại và hiển thị
        loadUserInfo();

        // Xử lý nút back
        if (backButtonPersonal != null) {
            backButtonPersonal.setOnClickListener(v -> finish());
        }

        // Xử lý nút Lưu thông tin
        if (saveInfoButton != null) { // Đảm bảo nút không null
            saveInfoButton.setOnClickListener(v -> saveUserInfo());
        }


        // TODO: Xử lý click vào ảnh đại diện để thay đổi (chức năng phức tạp hơn, cần quyền truy cập bộ nhớ và chọn ảnh)
        if (personalAvatarImageView != null) { // Đảm bảo ImageView không null
            personalAvatarImageView.setOnClickListener(v -> {
                Toast.makeText(PersonalActivity.this, "Chức năng thay đổi ảnh đại diện đang được phát triển!", Toast.LENGTH_SHORT).show();
            });
        }
    }

    private void loadUserInfo() {
        currentUser = userDAO.getUserById(userId);
        if (currentUser != null) {
            fullNameEditText.setText(currentUser.getFullName());
            emailEditText.setText(currentUser.getEmail());
            phoneNumberEditText.setText(currentUser.getPhone());
            addressEditText.setText(currentUser.getAddress());

            // TODO: Load ảnh đại diện nếu có (cần lưu đường dẫn ảnh trong User model và có logic tải ảnh)
            // personalAvatarImageView.setImageResource(R.drawable.ava); // Ảnh mặc định nếu chưa có ảnh riêng
        } else {
            Toast.makeText(this, "Không thể tải thông tin cá nhân.", Toast.LENGTH_SHORT).show();
        }
    }

    private void saveUserInfo() {
        String newFullName = fullNameEditText.getText().toString().trim();
        String newEmail = emailEditText.getText().toString().trim();
        String newPhoneNumber = phoneNumberEditText.getText().toString().trim();
        String newAddress = addressEditText.getText().toString().trim();

        // Kiểm tra dữ liệu hợp lệ (tùy chỉnh theo yêu cầu)
        if (newFullName.isEmpty() || newEmail.isEmpty() || newPhoneNumber.isEmpty() || newAddress.isEmpty()) {
            Toast.makeText(this, "Vui lòng nhập đầy đủ thông tin!", Toast.LENGTH_SHORT).show();
            return;
        }

        // Cập nhật đối tượng User hiện tại
        currentUser.setFullName(newFullName);
        currentUser.setEmail(newEmail);
        currentUser.setPhone(newPhoneNumber);
        currentUser.setAddress(newAddress);

        // Gọi DAO để cập nhật vào database
        boolean success = userDAO.updateUserInfo(currentUser);

        if (success) {
            Toast.makeText(this, "Cập nhật thông tin thành công!", Toast.LENGTH_SHORT).show();
            // Quay lại AccountActivity hoặc MainActivity sau khi lưu
            finish(); // Đóng ActivityPersonal
        } else {
            Toast.makeText(this, "Cập nhật thông tin thất bại. Vui lòng thử lại.", Toast.LENGTH_SHORT).show();
        }
    }
}