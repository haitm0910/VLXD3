// File: ChangePassActivity.java
package com.example.vlxd3;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.vlxd3.dao.UserDAO;
import com.example.vlxd3.model.User;

public class ChangePassActivity extends AppCompatActivity {

    private EditText currentPasswordEditText, newPasswordEditText, confirmNewPasswordEditText;
    private Button savePasswordButton;
    private ImageView backButtonChangePassword;

    private int userId;
    private UserDAO userDAO;
    private User currentUser; // Đối tượng người dùng hiện tại để kiểm tra mật khẩu cũ

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_change_pass); // Đảm bảo layout này tồn tại

        // Ánh xạ Views
        currentPasswordEditText = findViewById(R.id.et_current_password);
        newPasswordEditText = findViewById(R.id.et_new_password);
        confirmNewPasswordEditText = findViewById(R.id.et_comfirm_password); // Lỗi chính tả: et_comfirm_password thay vì et_confirm_password
        savePasswordButton = findViewById(R.id.btn_save_password);
        backButtonChangePassword = findViewById(R.id.iv_back_arrow_change_password);

        // Kiểm tra để tránh NullPointerException nếu nút không tìm thấy
        backButtonChangePassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish(); // Phương thức này sẽ đóng Activity hiện tại và quay về Activity trước đó
            }
        });

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

        // Tải thông tin người dùng hiện tại để có mật khẩu cũ
        currentUser = userDAO.getUserById(userId);
        if (currentUser == null) {
            Toast.makeText(this, "Không thể tải thông tin người dùng để đổi mật khẩu.", Toast.LENGTH_SHORT).show();
            finish(); // Đóng Activity nếu không lấy được thông tin user
            return;
        }

        // Xử lý nút back
        if (backButtonChangePassword != null) {
            backButtonChangePassword.setOnClickListener(v -> finish());
        }

        // Xử lý nút "Lưu thay đổi"
        if (savePasswordButton != null) {
            savePasswordButton.setOnClickListener(v -> changePassword());
        }
    }

    private void changePassword() {
        String currentPassword = currentPasswordEditText.getText().toString().trim();
        String newPassword = newPasswordEditText.getText().toString().trim();
        String confirmNewPassword = confirmNewPasswordEditText.getText().toString().trim();

        if (currentPassword.isEmpty() || newPassword.isEmpty() || confirmNewPassword.isEmpty()) {
            Toast.makeText(this, "Vui lòng nhập đầy đủ thông tin!", Toast.LENGTH_SHORT).show();
            return;
        }

        // 1. Kiểm tra mật khẩu hiện tại có đúng không
        if (!currentPassword.equals(currentUser.getPassword())) {
            Toast.makeText(this, "Mật khẩu hiện tại không đúng!", Toast.LENGTH_SHORT).show();
            return;
        }

        // 2. Kiểm tra mật khẩu mới và xác nhận mật khẩu mới có khớp không
        if (!newPassword.equals(confirmNewPassword)) {
            Toast.makeText(this, "Mật khẩu mới và xác nhận mật khẩu không khớp!", Toast.LENGTH_SHORT).show();
            return;
        }

        // 3. Kiểm tra mật khẩu mới có khác mật khẩu cũ không
        if (newPassword.equals(currentPassword)) {
            Toast.makeText(this, "Mật khẩu mới không được trùng với mật khẩu hiện tại!", Toast.LENGTH_SHORT).show();
            return;
        }

        // Gọi DAO để cập nhật mật khẩu
        // UserDAO.updatePassword yêu cầu username, nhưng chúng ta có userId
        // Tuy nhiên, UserDAO.updatePassword cũng có thể dùng userId nếu sửa lại,
        // hoặc chúng ta dùng username từ currentUser
        boolean success = userDAO.updatePassword(currentUser.getUsername(), newPassword);

        if (success) {
            Toast.makeText(this, "Đổi mật khẩu thành công!", Toast.LENGTH_LONG).show();
            // Sau khi đổi mật khẩu, có thể yêu cầu người dùng đăng nhập lại hoặc quay về màn hình tài khoản
            // Quay về AccountActivity và đóng ChangePassActivity
            finish();
        } else {
            Toast.makeText(this, "Đổi mật khẩu thất bại. Vui lòng thử lại.", Toast.LENGTH_SHORT).show();
        }
    }
}