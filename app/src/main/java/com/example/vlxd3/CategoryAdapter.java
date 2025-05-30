package com.example.vlxd3;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.vlxd3.model.Category;

import java.util.List;

public class CategoryAdapter extends BaseAdapter {
    private Context context;
    private List<Category> categoryList;

    public CategoryAdapter(Context context, List<Category> categoryList) {
        this.context = context;
        this.categoryList = categoryList;
    }

    @Override
    public int getCount() {
        return categoryList.size();
    }

    @Override
    public Object getItem(int position) {
        return categoryList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return categoryList.get(position).getId();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.list_single_category, parent, false);
        }
        TextView itemText = convertView.findViewById(R.id.item_text);
        ImageView itemImage = convertView.findViewById(R.id.item_image);
        Category category = categoryList.get(position);
        itemText.setText(category.getName());
        if (category.getImage() != null && !category.getImage().isEmpty()) {
            if (category.getImage().startsWith("content://") || category.getImage().startsWith("file://")) {
                itemImage.setImageURI(android.net.Uri.parse(category.getImage()));
            } else {
                int resId = context.getResources().getIdentifier(category.getImage(), "drawable", context.getPackageName());
                if (resId != 0) {
                    itemImage.setImageResource(resId);
                } else {
                    itemImage.setImageResource(R.drawable.logo); // Ảnh mặc định nếu không tìm thấy
                }
            }
        } else {
            itemImage.setImageResource(R.drawable.logo); // Ảnh mặc định nếu không có image
        }
        return convertView;
    }
}
