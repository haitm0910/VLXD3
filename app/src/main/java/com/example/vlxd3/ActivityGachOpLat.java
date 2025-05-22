// File: ActivityGachOpLat.java

package com.example.vlxd3;

import android.os.Bundle;
import android.widget.ListView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.vlxd3.dao.ProductDAO;
import com.example.vlxd3.model.Product;

import java.util.List;

public class ActivityGachOpLat extends AppCompatActivity {
    private ListView listViewProducts;
    private ProductDAO productDAO;
    private List<Product> productList;
    private ProductAdapter adapter;
    private int userId; // <-- KHAI BÁO BIẾN userId

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gach_op_lat);

        // NHẬN userId TỪ INTENT
        userId = getIntent().getIntExtra("userId", -1); // <-- THÊM DÒNG NÀY

        listViewProducts = findViewById(R.id.listViewProducts);
        int categoryId = getIntent().getIntExtra("categoryId", -1);
        productDAO = new ProductDAO(this);
        productList = productDAO.getProductsByCategory(categoryId);

        // TRUYỀN userId VÀO CONSTRUCTOR CỦA ProductAdapter
        adapter = new ProductAdapter(this, productList, userId); // <-- THÊM userId VÀO ĐÂY
        listViewProducts.setAdapter(adapter);
    }
}