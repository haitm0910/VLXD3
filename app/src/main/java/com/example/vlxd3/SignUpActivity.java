// File: SignUpActivity.java

package com.example.vlxd3;

import android.content.Intent; // Thêm import này
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.vlxd3.dao.UserDAO;
import com.example.vlxd3.model.User;

public class SignUpActivity extends AppCompatActivity {
    private EditText editTextUsername, editTextPassword, editTextConfirmPassword;
    private Button buttonSignUp;
    private UserDAO userDAO;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        editTextUsername = findViewById(R.id.editTextUsername);
        editTextPassword = findViewById(R.id.editTextPassword);
        editTextConfirmPassword = findViewById(R.id.editTextConfirmPassword);
        buttonSignUp = findViewById(R.id.buttonSignUp);
        userDAO = new UserDAO(this);

        buttonSignUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String username = editTextUsername.getText().toString().trim();
                String password = editTextPassword.getText().toString().trim();
                String confirmPassword = editTextConfirmPassword.getText().toString().trim();
                if (username.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
                    Toast.makeText(SignUpActivity.this, "Vui lòng nhập đầy đủ thông tin!", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (!password.equals(confirmPassword)) {
                    Toast.makeText(SignUpActivity.this, "Mật khẩu không khớp!", Toast.LENGTH_SHORT).show();
                    return;
                }
                User user = new User(username, password, username, ""); // fullName và phone tạm thời để trống
                long id = userDAO.registerUser(user);
                if (id > 0) {
                    Toast.makeText(SignUpActivity.this, "Đăng ký thành công!", Toast.LENGTH_SHORT).show();
                    // Chuyển về LoginActivity để người dùng đăng nhập
                    // Bạn có thể cân nhắc chuyển thẳng đến MainActivity và truyền userId nếu muốn
                    // Ví dụ:
                    // Intent intent = new Intent(SignUpActivity.this, MainActivity.class);
                    // intent.putExtra("userId", (int) id);
                    // startActivity(intent);
                    finish();
                } else {
                    Toast.makeText(SignUpActivity.this, "Tên đăng nhập đã tồn tại!", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
}