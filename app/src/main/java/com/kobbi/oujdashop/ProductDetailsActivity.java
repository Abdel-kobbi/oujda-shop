package com.kobbi.oujdashop;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.switchmaterial.SwitchMaterial;
import com.kobbi.oujdashop.Database.Database;
import com.kobbi.oujdashop.Models.Product;
import com.kobbi.oujdashop.Models.User;

import java.util.Objects;

public class ProductDetailsActivity extends AppCompatActivity {

    private TextView productName, productDesc, productPrice;

    private SwitchMaterial switchFavorite;

    private Database db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_product_details);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.detailsLayout), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // add toolbar to main activity
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        db = new Database(getApplicationContext());

        productName = findViewById(R.id.productName);
        productPrice = findViewById(R.id.productPrice);
        productDesc = findViewById(R.id.productDesc);
        switchFavorite = findViewById(R.id.addToFavorites);

        // get Product from intent
        Intent intent = getIntent();

        Product product = (Product) intent.getSerializableExtra("product");
        // get user info
        SharedPreferences sharedPreferences = getSharedPreferences("login", MODE_PRIVATE);

        int idUser = sharedPreferences.getInt("user_id", 0);
        User user = db.findUser(idUser);

        if (idUser == 0 || user == null) {
            Intent intentLogin = new Intent(getApplicationContext(), LoginActivity.class);
            startActivity(intentLogin);
            finish();
        }

        if (product != null && user != null) {
            productName.setText(product.getName());
            productPrice.setText(String.valueOf(product.getPrice()));
            productDesc.setText(product.getDescription());
            Objects.requireNonNull(getSupportActionBar()).setTitle(product.getName());
            switchFavorite.setChecked(db.isFavorite(user.getId(), product.getId()));

            // add change listener to add or delete favorite
            switchFavorite.setOnCheckedChangeListener((buttonView, isChecked) -> {
                if (isChecked) {
                    db.addFavorites(user.getId(), product.getId());
                } else {
                    db.deleteFavorites(user.getId(), product.getId());
                }
            });
        }

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.details_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.profile) {
            // navigate to profile activity
            Intent intent = new Intent(getApplicationContext(), ProfileActivity.class);
            startActivity(intent);
            return true;
        } else if (item.getItemId() == R.id.favorites) {
            // navigate to favorites activity
            Intent intent = new Intent(getApplicationContext(), FavoritesActivity.class);
            startActivity(intent);
            return true;
        } else if (item.getItemId() == R.id.logout) {
            // logout and navigate to login activity
            SharedPreferences sharedPreferences = getSharedPreferences("login", MODE_PRIVATE);
            sharedPreferences.edit().clear().apply();
            Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
            startActivity(intent);
            finish();
            return true;
        } else {
            return super.onOptionsItemSelected(item);
        }
    }
}