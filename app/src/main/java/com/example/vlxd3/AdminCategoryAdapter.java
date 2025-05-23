// File: AdminCategoryAdapter.java

package com.example.vlxd3;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog; // Import này cho Dialog
import androidx.recyclerview.widget.RecyclerView;

import com.example.vlxd3.dao.CategoryDAO;
import com.example.vlxd3.model.Category;

import java.util.List;

public class AdminCategoryAdapter extends RecyclerView.Adapter<AdminCategoryAdapter.AdminCategoryViewHolder> {

    private Context context;
    private List<Category> categoryList;
    private CategoryDAO categoryDAO;
    private OnCategoryActionListener listener; // Interface để thông báo Activity

    // Định nghĩa interface để thông báo khi có hành động
    public interface OnCategoryActionListener {
        void onCategoryDeleted(); // Khi một danh mục bị xóa
        void onCategoryClicked(int categoryId, String categoryName); // Khi một danh mục được click
    }

    public AdminCategoryAdapter(Context context, List<Category> categoryList, OnCategoryActionListener listener) {
        this.context = context;
        this.categoryList = categoryList;
        this.categoryDAO = new CategoryDAO(context);
        this.listener = listener;
    }

    @NonNull
    @Override
    public AdminCategoryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.list_admin_category_item, parent, false);
        return new AdminCategoryViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull AdminCategoryViewHolder holder, int position) {
        Category category = categoryList.get(position);
        holder.categoryName.setText(category.getName());

        // Xử lý hiển thị ảnh danh mục
        if (category.getImage() != null && !category.getImage().isEmpty()) {
            int imageResId = context.getResources().getIdentifier(category.getImage(), "drawable", context.getPackageName());
            if (imageResId != 0) {
                holder.categoryImage.setImageResource(imageResId);
            } else {
                holder.categoryImage.setImageResource(R.drawable.logo); // Fallback
            }
        } else {
            holder.categoryImage.setImageResource(R.drawable.logo); // Mặc định là logo nếu không có ảnh
        }

        // Xử lý nút xóa danh mục
        holder.deleteButton.setOnClickListener(v -> {
            // Hiển thị dialog xác nhận trước khi xóa
            new AlertDialog.Builder(context)
                    .setTitle("Xác nhận xóa danh mục")
                    .setMessage("Bạn có chắc muốn xóa danh mục '" + category.getName() + "'? Hành động này sẽ xóa TẤT CẢ sản phẩm thuộc danh mục này.")
                    .setPositiveButton("Xóa", (dialog, which) -> {
                        boolean success = categoryDAO.deleteCategory(category.getId());
                        if (success) {
                            Toast.makeText(context, "Đã xóa danh mục: " + category.getName(), Toast.LENGTH_SHORT).show();
                            // Thông báo cho Activity để làm mới danh sách
                            if (listener != null) {
                                listener.onCategoryDeleted();
                            }
                        } else {
                            Toast.makeText(context, "Xóa danh mục thất bại!", Toast.LENGTH_SHORT).show();
                        }
                    })
                    .setNegativeButton("Hủy", null)
                    .show();
        });

        // Xử lý click vào item danh mục để xem sản phẩm
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onCategoryClicked(category.getId(), category.getName());
            }
        });
    }

    @Override
    public int getItemCount() {
        return categoryList.size();
    }

    public static class AdminCategoryViewHolder extends RecyclerView.ViewHolder {
        ImageView categoryImage;
        TextView categoryName;
        ImageView deleteButton;

        public AdminCategoryViewHolder(@NonNull View itemView) {
            super(itemView);
            categoryImage = itemView.findViewById(R.id.admin_category_image);
            categoryName = itemView.findViewById(R.id.admin_category_name);
            deleteButton = itemView.findViewById(R.id.deleteCategoryButton);
        }
    }
}