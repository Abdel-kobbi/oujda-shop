package com.kobbi.oujdashop;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.kobbi.oujdashop.Models.Category;

import java.util.List;

public class CategoryAdapter extends ArrayAdapter<Category> {

    private Context context;
    private int resource;
    public CategoryAdapter(@NonNull Context context, int resource, @NonNull List<Category> categories) {
        super(context, resource, categories);
        this.context = context;
        this.resource = resource;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        convertView = LayoutInflater.from(context).inflate(resource, parent, false);
        TextView categoryName = (TextView) convertView.findViewById(R.id.title);
        TextView categoryPhone = (TextView) convertView.findViewById(R.id.description);
        Category currentCategory = getItem(position);
        categoryName.setText(currentCategory.getName());
        categoryPhone.setText(currentCategory.getDescription());
        return convertView;
    }
}
