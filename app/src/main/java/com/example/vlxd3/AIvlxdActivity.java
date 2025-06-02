package com.example.vlxd3;

import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.vlxd3.dao.CartDAO;
import com.example.vlxd3.dao.ProductDAO;
import com.example.vlxd3.model.CartItem;
import com.example.vlxd3.model.Product;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class AIvlxdActivity extends AppCompatActivity {
    private EditText editTextCost, editTextArea;
    private Spinner spinnerStyle, spinnerPurpose;
    private Button btnSuggest, btnOrder;
    private ListView listViewSuggestions;
    private ArrayAdapter<String> suggestionAdapter;
    private List<Product> suggestedProducts = new ArrayList<>();
    private int userId;
    private ProductDAO productDAO;
    private CartDAO cartDAO;
    private static final String GEMINI_API_KEY = "AIzaSyDdqOjb7yDkp3YckgdyTjh_YoEfxn-POFc"; // TODO: Replace with your Gemini API key
    private static final String GEMINI_API_URL = "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.0-flash:generateContent?key=";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_aivlxd);

        editTextCost = findViewById(R.id.editTextCost);
        editTextArea = findViewById(R.id.editTextArea);
        spinnerPurpose = findViewById(R.id.spinnerPurpose);
        ArrayAdapter<CharSequence> purposeAdapter = ArrayAdapter.createFromResource(this,
                R.array.purpose_array, android.R.layout.simple_spinner_item);
        purposeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerPurpose.setAdapter(purposeAdapter);
        spinnerStyle = findViewById(R.id.spinnerStyle);
        btnSuggest = findViewById(R.id.btnSuggest);
        btnOrder = findViewById(R.id.btnOrder);
        listViewSuggestions = findViewById(R.id.listViewSuggestions);
        suggestionAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, new ArrayList<>());
        listViewSuggestions.setAdapter(suggestionAdapter);
        userId = getIntent().getIntExtra("userId", -1);
        productDAO = new ProductDAO(this);
        cartDAO = new CartDAO(this);

        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.style_array, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerStyle.setAdapter(adapter);

        btnSuggest.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String cost = editTextCost.getText().toString().trim();
                String area = editTextArea.getText().toString().trim();
                String purpose = spinnerPurpose.getSelectedItem().toString();
                String style = spinnerStyle.getSelectedItem().toString();
                if (cost.isEmpty() || area.isEmpty()) {
                    Toast.makeText(AIvlxdActivity.this, "Vui lòng nhập đầy đủ thông tin!", Toast.LENGTH_SHORT).show();
                    return;
                }
                // Lấy danh sách sản phẩm từ database
                List<Product> allProducts = productDAO.getAllProducts();
                StringBuilder productListStr = new StringBuilder();
                for (Product p : allProducts) {
                    productListStr.append("- ").append(p.getName()).append(" (" + p.getUnit() + ")\n");
                }
                String prompt = "Tôi có " + cost + " VNĐ, diện tích " + area + " m2, mục đích: " + purpose + ", phong cách: " + style + ". Dưới đây là danh sách sản phẩm vật liệu xây dựng trong kho: \n" + productListStr + "\nHãy chọn ra các sản phẩm phù hợp nhất từ danh sách trên (chỉ trả về tên sản phẩm, phân tách bằng dấu phẩy, không giải thích).";
                new GeminiSuggestTask().execute(prompt);
            }
        });
        btnOrder.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (suggestedProducts.isEmpty()) {
                    Toast.makeText(AIvlxdActivity.this, "Chưa có sản phẩm gợi ý để lên đơn!", Toast.LENGTH_SHORT).show();
                    return;
                }
                for (Product p : suggestedProducts) {
                    cartDAO.addToCart(new CartItem(userId, p.getId(), 1));
                }
                Toast.makeText(AIvlxdActivity.this, "Đã thêm tất cả sản phẩm gợi ý vào giỏ hàng!", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private class GeminiSuggestTask extends AsyncTask<String, Void, List<Product>> {
        @Override
        protected List<Product> doInBackground(String... prompts) {
            List<Product> matchedProducts = new ArrayList<>();
            try {
                URL url = new URL(GEMINI_API_URL + GEMINI_API_KEY);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
                conn.setDoOutput(true);
                JSONObject body = new JSONObject();
                JSONArray contents = new JSONArray();
                JSONObject part = new JSONObject();
                part.put("text", prompts[0]);
                JSONObject content = new JSONObject();
                content.put("parts", new JSONArray().put(part));
                contents.put(content);
                body.put("contents", contents);
                OutputStream os = conn.getOutputStream();
                os.write(body.toString().getBytes("UTF-8"));
                os.close();
                BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                StringBuilder sb = new StringBuilder();
                String line;
                while ((line = br.readLine()) != null) sb.append(line);
                br.close();
                String response = sb.toString();
                // Parse Gemini response (expecting product names separated by commas)
                JSONObject json = new JSONObject(response);
                JSONArray candidates = json.getJSONArray("candidates");
                String text = candidates.getJSONObject(0).getJSONObject("content").getJSONArray("parts").getJSONObject(0).getString("text");
                String[] names = text.split(",");
                List<Product> allProducts = productDAO.getAllProducts();
                for (String name : names) {
                    String trimmed = name.trim();
                    for (Product p : allProducts) {
                        if (p.getName().equalsIgnoreCase(trimmed) || p.getName().toLowerCase().contains(trimmed.toLowerCase())) {
                            matchedProducts.add(p);
                        }
                    }
                }
            } catch (Exception e) {
                Log.e("AIvlxdActivity", "Gemini API error: " + e.getMessage(), e);
            }
            return matchedProducts;
        }
        @Override
        protected void onPostExecute(List<Product> products) {
            suggestedProducts.clear();
            suggestionAdapter.clear();
            if (products.isEmpty()) {
                suggestionAdapter.add("Không tìm thấy sản phẩm phù hợp trong kho!");
            } else {
                for (Product p : products) {
                    suggestionAdapter.add(p.getName());
                }
                suggestedProducts.addAll(products);
            }
            suggestionAdapter.notifyDataSetChanged();
        }
    }
}
