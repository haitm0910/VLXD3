// File: ProductAdapter.java

package com.example.vlxd3;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.vlxd3.model.Product;

import java.util.List;

public class ProductAdapter extends BaseAdapter {
    private Context context;
    private List<Product> productList;
    private int userId; // <-- KHAI BÁO BIẾN userId

    // SỬA CONSTRUCTOR ĐỂ NHẬN userId
    public ProductAdapter(Context context, List<Product> productList, int userId) { // <-- THÊM userId Ở ĐÂY
        this.context = context;
        this.productList = productList;
        this.userId = userId; // <-- GÁN userId VÀO BIẾN THÀNH VIÊN
    }

    @Override
    public int getCount() {
        return productList.size();
    }

    @Override
    public Object getItem(int position) {
        return productList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return productList.get(position).getId();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.list_gach_op_lat, parent, false);
        }
        TextView itemName = convertView.findViewById(R.id.product_name);
        TextView itemPrice = convertView.findViewById(R.id.product_price);
        ImageView itemImage = convertView.findViewById(R.id.product_image);
        Product product = productList.get(position);
        itemName.setText(product.getName());
        itemPrice.setText(product.getPrice() + " đ / " + product.getUnit());
        if (product.getImage() != null && !product.getImage().isEmpty()) {
            if (product.getImage().startsWith("content://") || product.getImage().startsWith("file://")) {
                itemImage.setImageURI(android.net.Uri.parse(product.getImage()));
            } else {
                int resId = context.getResources().getIdentifier(product.getImage(), "drawable", context.getPackageName());
                if (resId != 0) {
                    itemImage.setImageResource(resId);
                } else {
                    itemImage.setImageResource(R.drawable.logo);
                }
            }
        } else {
            itemImage.setImageResource(R.drawable.logo);
        }
        convertView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, ActivityProductDetail.class);
                intent.putExtra("productId", product.getId());
                intent.putExtra("userId", userId); // <-- TRUYỀN userId
                context.startActivity(intent);
            }
        });
        return convertView;
    }
}