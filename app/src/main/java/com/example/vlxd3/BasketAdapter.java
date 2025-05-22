package com.example.vlxd3;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.vlxd3.model.CartItem;
import com.example.vlxd3.model.Product;

import java.util.List;

public class BasketAdapter extends BaseAdapter {
    private Context context;
    private List<CartItem> cartItems;
    private List<Product> products;

    public BasketAdapter(Context context, List<CartItem> cartItems, List<Product> products) {
        this.context = context;
        this.cartItems = cartItems;
        this.products = products;
    }

    @Override
    public int getCount() {
        return cartItems.size();
    }

    @Override
    public Object getItem(int position) {
        return cartItems.get(position);
    }

    @Override
    public long getItemId(int position) {
        return cartItems.get(position).getId();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.item_basket_product, parent, false);
        }
        TextView name = convertView.findViewById(R.id.product_name);
        TextView price = convertView.findViewById(R.id.product_price);
        TextView quantity = convertView.findViewById(R.id.quantity_text);
        ImageView image = convertView.findViewById(R.id.product_image);
        Product product = products.get(position);
        CartItem item = cartItems.get(position);
        name.setText(product.getName());
        price.setText(product.getPrice() + " đ");
        quantity.setText("x" + item.getQuantity());
        // image.setImageResource(...) // Để bạn tự thêm ảnh sau
        return convertView;
    }
}
