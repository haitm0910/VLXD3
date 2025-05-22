// File: CategoryMainAdapter.java
package com.example.vlxd3;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast; // Import Toast nếu bạn muốn báo lỗi userId

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.vlxd3.model.Category;

import java.util.List;

public class CategoryMainAdapter extends RecyclerView.Adapter<CategoryMainAdapter.CategoryViewHolder> {

    private Context context;
    private List<Category> categoryList;
    private int userId; // Để truyền userId khi click vào danh mục

    public CategoryMainAdapter(Context context, List<Category> categoryList, int userId) {
        this.context = context;
        this.categoryList = categoryList;
        this.userId = userId;
    }

    @NonNull
    @Override
    public CategoryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Sử dụng layout item cho danh mục trên màn hình chính
        View view = LayoutInflater.from(context).inflate(R.layout.list_category_main_screen, parent, false);
        return new CategoryViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CategoryViewHolder holder, int position) {
        Category category = categoryList.get(position);
        holder.categoryName.setText(category.getName());

        // Xử lý hiển thị ảnh danh mục
        // category.getImage() sẽ trả về tên resource drawable (ví dụ: "icon_cat_sat")
        // Bạn cần đảm bảo đã có các drawable tương ứng trong thư mục res/drawable
        if (category.getImage() != null && !category.getImage().isEmpty()) {
            int imageResId = context.getResources().getIdentifier(category.getImage(), "drawable", context.getPackageName());
            if (imageResId != 0) { // Nếu tìm thấy resource
                holder.categoryIcon.setImageResource(imageResId);
            } else {
                holder.categoryIcon.setImageResource(R.drawable.logo); // Fallback về ảnh mặc định
            }
        } else {
            holder.categoryIcon.setImageResource(R.drawable.logo); // Mặc định là logo nếu không có ảnh
        }


        holder.itemView.setOnClickListener(v -> {
            if (userId == -1) {
                Toast.makeText(context, "Vui lòng đăng nhập để xem sản phẩm!", Toast.LENGTH_SHORT).show();
                // Có thể chuyển về LoginActivity nếu muốn:
                // Intent loginIntent = new Intent(context, LoginActivity.class);
                // context.startActivity(loginIntent);
                return;
            }
            // Chức năng giống như trong ActivityCategory
            Intent intent = new Intent(context, ActivityGachOpLat.class);
            intent.putExtra("categoryId", category.getId());
            intent.putExtra("userId", userId); // Truyền userId
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return categoryList.size();
    }

    public static class CategoryViewHolder extends RecyclerView.ViewHolder {
        TextView categoryName;
        ImageView categoryIcon;

        public CategoryViewHolder(@NonNull View itemView) {
            super(itemView);
            categoryName = itemView.findViewById(R.id.textViewCriteriaName);
            categoryIcon = itemView.findViewById(R.id.imageViewCriteriaIcon);
        }
    }
}