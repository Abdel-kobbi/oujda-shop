package com.kobbi.oujdashop;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.ContextThemeWrapper;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.snackbar.Snackbar;
import com.kobbi.oujdashop.Adapters.ProductAdapter;
import com.kobbi.oujdashop.Database.Database;
import com.kobbi.oujdashop.Models.Category;
import com.kobbi.oujdashop.Models.Product;

import java.util.List;

public class ProductActivity extends AppCompatActivity {

    private GridView gridViewProduct;

    private Database db;

    private Category categoryProduct;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_product);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.productLayout), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // add toolbar to product activity
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        db = new Database(getApplicationContext());

        gridViewProduct = findViewById(R.id.gridViewProduct);

        gridViewProduct.setEmptyView(findViewById(R.id.emptyListProduct));

        // get category product
        Category category = (Category) getIntent().getSerializableExtra("category");

        if (category != null) {
            categoryProduct = category;
        } else {
            Intent intent = new Intent(getApplicationContext(), MainActivity.class);
            startActivity(intent);
        }

        loadProduct();
    }

    private void loadProduct() {
        List<Product> products = db.getProductBuCategory(categoryProduct);
        ProductAdapter adapter = new ProductAdapter(getApplicationContext(), R.layout.item_product, products);
        gridViewProduct.setAdapter(adapter);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.add) {
            // show dialog to add new product
            showAddProductDialog();
            return true;
        } else if (item.getItemId() == R.id.profile) {
            // navigate to profile activity
            Intent intent = new Intent(getApplicationContext(), ProfileActivity.class);
            startActivity(intent);
            return true;
        } else if (item.getItemId() == R.id.logout) {
            // logout and navigate to login activity
            SharedPreferences sharedPreferences = getSharedPreferences("isLogin", MODE_PRIVATE);
            sharedPreferences.edit().clear().apply();
            Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
            startActivity(intent);
            finish();
            return true;
        } else {
            return super.onOptionsItemSelected(item);
        }
    }

    private void showAddProductDialog() {
        AlertDialog.Builder builderAlert = new AlertDialog.Builder(new ContextThemeWrapper(this, androidx.appcompat.R.style.Theme_AppCompat_Dialog_Alert));
        AlertDialog alertDialog = builderAlert.create();
        alertDialog.setTitle("Ajouter produit");
        LayoutInflater inflater = getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.add_product_layout, null);
        alertDialog.setView(dialogView);

        EditText productName = dialogView.findViewById(R.id.productName);
        EditText productPrice = dialogView.findViewById(R.id.productPrice);
        EditText productDesc = dialogView.findViewById(R.id.productDesc);

        Button btnAdd = dialogView.findViewById(R.id.btnAdd);
        Button btnCancel = dialogView.findViewById(R.id.btnCancel);


        btnAdd.setOnClickListener(v -> {
            String name = productName.getText().toString().trim();
            String price = productPrice.getText().toString().trim();
            String desc = productDesc.getText().toString().trim();
            if (name.isEmpty() || desc.isEmpty() || price.isEmpty()) {
                Toast.makeText(getApplicationContext(), "Tous les champs sont obligatoire!", Toast.LENGTH_SHORT).show();
                return;
            }

            if (!isDouble(price)) {
                Toast.makeText(getApplicationContext(), "Le prix doit être un nombre réelle!", Toast.LENGTH_SHORT).show();
                return;
            }

            boolean isAdded = db.addProduct(new Product(name, Double.parseDouble(price), desc, categoryProduct));
            if (isAdded) {
                alertDialog.dismiss();
                Snackbar.make(findViewById(R.id.productLayout), "La produit '" + name + "' a été ajoutée avec succès.", Snackbar.LENGTH_LONG).show();
                loadProduct();
            } else {
                Snackbar.make(findViewById(R.id.productLayout), "Échec de l'ajout, Veuillez réessayer.", Snackbar.LENGTH_LONG).show();
            }
        });

        btnCancel.setOnClickListener(v -> {
            alertDialog.dismiss();
        });

        alertDialog.show();
    }

    private boolean isDouble(String value) {
        try {
            Double.parseDouble(value);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}