// File: ForgetPassActivity.java
package com.example.vlxd3;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.vlxd3.dao.UserDAO;

public class ForgetPassActivity extends AppCompatActivity {

    private EditText usernameEditText, newPasswordEditText, confirmNewPasswordEditText;
    private Button changePasswordButton;
    private TextView loginLinkTextView; // Để quay lại trang đăng nhập

    private UserDAO userDAO;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forget_pass); // Đảm bảo layout này tồn tại

        // Ánh xạ Views
        usernameEditText = findViewById(R.id.editText_Username);
        newPasswordEditText = findViewById(R.id.editTextNewPassword);
        confirmNewPasswordEditText = findViewById(R.id.editTextConfirmNewPassword);
        changePasswordButton = findViewById(R.id.buttonSignUp); // ID này trong layout của bạn là buttonSignUp, có thể nên đổi tên cho rõ ràng hơn
        loginLinkTextView = findViewById(R.id.textViewLoginLink);

        // Khởi tạo DAO
        userDAO = new UserDAO(this);

        // Xử lý nút "Đổi mật khẩu"
        changePasswordButton.setOnClickListener(v -> changePassword());

        // Xử lý link "Đăng nhập"
        loginLinkTextView.setOnClickListener(v -> {
            Intent intent = new Intent(ForgetPassActivity.this, LoginActivity.class);
            startActivity(intent);
            finish(); // Đóng ForgetPassActivity
        });
    }

    private void changePassword() {
        String username = usernameEditText.getText().toString().trim();
        String newPassword = newPasswordEditText.getText().toString().trim();
        String confirmPassword = confirmNewPasswordEditText.getText().toString().trim();

        if (username.isEmpty() || newPassword.isEmpty() || confirmPassword.isEmpty()) {
            Toast.makeText(this, "Vui lòng nhập đầy đủ thông tin!", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!newPassword.equals(confirmPassword)) {
            Toast.makeText(this, "Mật khẩu mới và xác nhận mật khẩu không khớp!", Toast.LENGTH_SHORT).show();
            return;
        }

        // Kiểm tra xem tên đăng nhập có tồn tại không
        if (!userDAO.isUsernameExists(username)) { // Cần có phương thức isUsernameExists trong UserDAO
            Toast.makeText(this, "Tên đăng nhập không tồn tại!", Toast.LENGTH_SHORT).show();
            return;
        }

        // Cập nhật mật khẩu trong database
        boolean success = userDAO.updatePassword(username, newPassword);

        if (success) {
            Toast.makeText(this, "Đổi mật khẩu thành công! Vui lòng đăng nhập lại.", Toast.LENGTH_LONG).show();
            // Chuyển về màn hình đăng nhập
            Intent intent = new Intent(ForgetPassActivity.this, LoginActivity.class);
            startActivity(intent);
            finish(); // Đóng ForgetPassActivity
        } else {
            Toast.makeText(this, "Đổi mật khẩu thất bại. Vui lòng thử lại.", Toast.LENGTH_SHORT).show();
        }
    }
}