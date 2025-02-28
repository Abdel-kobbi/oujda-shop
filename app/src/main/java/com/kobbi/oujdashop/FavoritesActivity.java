package com.kobbi.oujdashop;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.GridView;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.kobbi.oujdashop.Adapters.ProductAdapter;
import com.kobbi.oujdashop.Database.Database;
import com.kobbi.oujdashop.Models.Product;

import java.util.List;
import java.util.Objects;

public class FavoritesActivity extends AppCompatActivity {

    private GridView gridViewProduct;

    private Database db;

    private List<Product> products;

    private int userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_favorites);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.favoritesLayout), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // add toolbar to favorite activity
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        Objects.requireNonNull(getSupportActionBar()).setTitle("Liste des favoris");

        db = new Database(getApplicationContext());

        gridViewProduct = findViewById(R.id.gridViewProduct);

        gridViewProduct.setEmptyView(findViewById(R.id.emptyListProduct));

        SharedPreferences sharedPreferences = getSharedPreferences("login", MODE_PRIVATE);

        userId = sharedPreferences.getInt("user_id", 0);

        loadProduct();
    }

    private void loadProduct() {
        products = db.getAllFavoritesProducts(userId);
        ProductAdapter adapter = new ProductAdapter(getApplicationContext(), R.layout.item_product, products);
        gridViewProduct.setAdapter(adapter);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.favorite_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        Intent intent;
        if (item.getItemId() == R.id.listCategories) {
            // navigate to category activity
            intent = new Intent(getApplicationContext(), MainActivity.class);
            startActivity(intent);
            return true;
        } else if (item.getItemId() == R.id.profile) {
            // navigate to profile activity
            intent = new Intent(getApplicationContext(), ProfileActivity.class);
            startActivity(intent);
            return true;
        } else if (item.getItemId() == R.id.logout) {
            // logout and navigate to login activity
            SharedPreferences sharedPreferences = getSharedPreferences("login", MODE_PRIVATE);
            sharedPreferences.edit().clear().apply();
            intent = new Intent(getApplicationContext(), LoginActivity.class);
            startActivity(intent);
            finish();
            return true;
        } else {
            return super.onOptionsItemSelected(item);
        }
    }
}