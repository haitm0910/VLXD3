// File: ActivityCategory.java

package com.example.vlxd3;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.vlxd3.dao.CategoryDAO;
import com.example.vlxd3.model.Category;

import java.util.ArrayList;
import java.util.List;

public class ActivityCategory extends AppCompatActivity {
    private ListView listViewCategories;
    private CategoryDAO categoryDAO;
    private List<Category> categoryList;
    private CategoryAdapter adapter;
    private int userId; // <-- KHAI BÁO BIẾN userId

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_category);

        // Thêm nút back (đã có trong file sửa lỗi trước)
        ImageView backButton = findViewById(R.id.backButtonCategory);
        if (backButton != null) {
            backButton.setOnClickListener(v -> finish());
        }

        // NHẬN userId TỪ INTENT
        userId = getIntent().getIntExtra("userId", -1); // <-- THÊM DÒNG NÀY

        listViewCategories = findViewById(R.id.listViewCategories);
        categoryDAO = new CategoryDAO(this);
        categoryList = categoryDAO.getAllCategories();
        List<String> categoryNames = new ArrayList<>();
        for (Category c : categoryList) {
            categoryNames.add(c.getName());
        }
        adapter = new CategoryAdapter(this, categoryList);
        listViewCategories.setAdapter(adapter);
        listViewCategories.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                int categoryId = categoryList.get(position).getId();
                Intent intent = new Intent(ActivityCategory.this, ActivityGachOpLat.class);
                intent.putExtra("categoryId", categoryId);
                intent.putExtra("userId", userId); // <-- TRUYỀN userId
                startActivity(intent);
            }
        });
    }
}