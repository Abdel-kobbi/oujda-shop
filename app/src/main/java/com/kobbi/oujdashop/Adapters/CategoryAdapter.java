package com.kobbi.oujdashop.Adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.kobbi.oujdashop.Models.Category;
import com.kobbi.oujdashop.R;

import java.util.List;

public class CategoryAdapter extends ArrayAdapter<Category> {

    private final Context context;
    private final int resource;

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
        TextView categoryDesc = (TextView) convertView.findViewById(R.id.description);
        Category currentCategory = getItem(position);
        categoryName.setText(currentCategory.getName());
        categoryDesc.setText(currentCategory.getDescription());
        return convertView;
    }
}
