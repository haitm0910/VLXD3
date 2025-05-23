package com.example.vlxd3.adapter;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.viewpager.widget.PagerAdapter;

import com.example.vlxd3.R; // Make sure this R is correctly imported

import java.util.List;

public class ProductImagePagerAdapter extends PagerAdapter {

    private Context context;
    private List<String> imageNames; // List of image filenames (e.g., "ximang_insee")
    private LayoutInflater layoutInflater;

    public ProductImagePagerAdapter(Context context, List<String> imageNames) {
        this.context = context;
        this.imageNames = imageNames;
        this.layoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public int getCount() {
        return imageNames.size(); // Number of images to display
    }

    @Override
    public boolean isViewFromObject(@NonNull View view, @NonNull Object object) {
        return view == object;
    }

    @NonNull
    @Override
    public Object instantiateItem(@NonNull ViewGroup container, int position) {
        View itemView = layoutInflater.inflate(R.layout.item_product_image, container, false);

        ImageView imageView = itemView.findViewById(R.id.imageViewProduct);

        String imageName = imageNames.get(position);
        if (imageName != null && !imageName.isEmpty()) {
            int imageResId = context.getResources().getIdentifier(imageName, "drawable", context.getPackageName());
            if (imageResId != 0) {
                imageView.setImageResource(imageResId);
            } else {
                imageView.setImageResource(R.drawable.logo); // Fallback to placeholder
                Log.e("ImagePagerAdapter", "Image resource not found for: " + imageName);
            }
        } else {
            imageView.setImageResource(R.drawable.logo); // Fallback for null/empty name
        }

        container.addView(itemView);
        return itemView;
    }

    @Override
    public void destroyItem(@NonNull ViewGroup container, int position, @NonNull Object object) {
        container.removeView((View) object);
    }
}