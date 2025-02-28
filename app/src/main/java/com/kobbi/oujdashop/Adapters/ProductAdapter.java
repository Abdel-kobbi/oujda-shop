package com.kobbi.oujdashop.Adapters;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.kobbi.oujdashop.Models.Product;
import com.kobbi.oujdashop.R;

import java.util.List;

public class ProductAdapter extends ArrayAdapter<Product> {

    private final Context context;
    private final int resource;

    public ProductAdapter(@NonNull Context context, int resource, @NonNull List<Product> products) {
        super(context, resource, products);
        this.context = context;
        this.resource = resource;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        convertView = LayoutInflater.from(context).inflate(resource, parent, false);
        TextView productName = (TextView) convertView.findViewById(R.id.productName);
        TextView productPrice = (TextView) convertView.findViewById(R.id.productPrice);
        ImageView productImg = (ImageView) convertView.findViewById(R.id.productImage);
        Product currentProduct = getItem(position);
        productName.setText(currentProduct.getName());
        productPrice.setText(String.format("%.2f DH", currentProduct.getPrice()));
        Bitmap bitmap = loadImageFromStorage(currentProduct.getImage());
        if (bitmap != null) {
            productImg.setImageBitmap(bitmap);
        }
        return convertView;
    }

    public Bitmap loadImageFromStorage(String path) {
        return BitmapFactory.decodeFile(path);
    }
}