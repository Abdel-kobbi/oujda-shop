package com.kobbi.oujdashop;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.switchmaterial.SwitchMaterial;
import com.kobbi.oujdashop.Models.Product;

import java.util.Objects;

public class DetailsActivity extends AppCompatActivity {

    TextView productName, productDesc, productPrice;

    SwitchMaterial switchFavorite;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_details);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.detailsLayout), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // add toolbar to main activity
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);


        productName = findViewById(R.id.productName);
        productPrice = findViewById(R.id.productPrice);
        productDesc = findViewById(R.id.productDesc);
        switchFavorite = findViewById(R.id.addToFavorites);

        Intent intent = getIntent();

        Product product = (Product) intent.getSerializableExtra("product");

        if (product != null) {
            productName.setText(product.getName());
            productPrice.setText(String.valueOf(product.getPrice()));
            productDesc.setText(product.getDescription());
            Objects.requireNonNull(getSupportActionBar()).setTitle(product.getName());
        }

    }
}