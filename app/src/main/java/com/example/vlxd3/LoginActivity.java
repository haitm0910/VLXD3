// File: LoginActivity.java
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
import com.example.vlxd3.model.User;

public class LoginActivity extends AppCompatActivity {
    private EditText editTextEmail, editTextPassword;
    private Button buttonLogin;
    private TextView textViewSignUp, textViewForgotPassword;
    private UserDAO userDAO;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        editTextEmail = findViewById(R.id.editTextEmail);
        editTextPassword = findViewById(R.id.editTextPassword);
        buttonLogin = findViewById(R.id.buttonLogin);
        textViewSignUp = findViewById(R.id.textViewSignUp);
        textViewForgotPassword = findViewById(R.id.textViewForgotPassword);
        userDAO = new UserDAO(this);

        buttonLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String username = editTextEmail.getText().toString().trim();
                String password = editTextPassword.getText().toString().trim();
                User user = userDAO.login(username, password);
                if (user != null) {
                    Toast.makeText(LoginActivity.this, "Đăng nhập thành công!", Toast.LENGTH_SHORT).show();
                    // KIỂM TRA VAI TRÒ (ROLE) CỦA NGƯỜI DÙNG
                    if ("admin".equals(user.getRole())) { // Nếu là admin
                        Intent intent = new Intent(LoginActivity.this, AdminDashboardActivity.class); // Chuyển đến Admin Dashboard
                        intent.putExtra("userId", user.getId()); // Truyền userId của admin
                        startActivity(intent);
                    } else { // Nếu là customer hoặc vai trò khác
                        Intent intent = new Intent(LoginActivity.this, MainActivity.class); // Chuyển đến MainActivity
                        intent.putExtra("userId", user.getId());
                        startActivity(intent);
                    }
                    finish();
                } else {
                    Toast.makeText(LoginActivity.this, "Sai tài khoản hoặc mật khẩu!", Toast.LENGTH_SHORT).show();
                }
            }
        });

        textViewSignUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(LoginActivity.this, SignUpActivity.class);
                startActivity(intent);
            }
        });

        if (textViewForgotPassword != null) {
            textViewForgotPassword.setOnClickListener(v -> {
                Intent intent = new Intent(LoginActivity.this, ForgetPassActivity.class);
                startActivity(intent);
            });
        }
    }
}