package com.kobbi.oujdashop;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.ContextMenu;
import android.view.ContextThemeWrapper;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
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
import java.util.Objects;

public class ProductActivity extends AppCompatActivity {

    private GridView gridViewProduct;

    private Database db;

    private Category categoryProduct;

    private List<Product> products;

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

        Objects.requireNonNull(getSupportActionBar()).setTitle("Liste des produits");


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

        gridViewProduct.setOnItemClickListener((parent, view, position, id) -> {
            Product selectedProduct = products.get(position);
            Intent intent = new Intent(getApplicationContext(), ProductDetailsActivity.class);
            intent.putExtra("product", selectedProduct);
            startActivity(intent);
        });

        // add context menu to grid view
        registerForContextMenu(gridViewProduct);

    }

    private void loadProduct() {
        products = db.getProductBuCategory(categoryProduct);
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

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        getMenuInflater().inflate(R.menu.context_menu, menu);
    }

    @Override
    public boolean onContextItemSelected(@NonNull MenuItem item) {
        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
        Product product = products.get(info.position);
        int itemSelected = item.getItemId();
        if (itemSelected == R.id.update) {
            showUpdateProductDialog(product);
            return true;
        } else if (itemSelected == R.id.delete) {
            showConfirmDeleteDialog(product);
            return true;
        } else {
            return super.onContextItemSelected(item);
        }
    }

    // dialog for add new product
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

            boolean isAdded = db.addProduct(new Product(name.substring(0, 1).toUpperCase() + name.substring(1).toLowerCase(), Double.parseDouble(price), desc, categoryProduct));
            if (isAdded) {
                alertDialog.dismiss();
                Snackbar.make(findViewById(R.id.productLayout), "La produit '" + name + "' a été ajoutée avec succès.", Snackbar.LENGTH_LONG).show();
                loadProduct();
            } else {
                Snackbar.make(findViewById(R.id.productLayout), "Échec de l'ajout, Veuillez réessayer.", Snackbar.LENGTH_LONG).show();
            }
        });

        btnCancel.setOnClickListener(v -> alertDialog.dismiss());

        alertDialog.show();
    }

    // dialog for update product;
    private void showUpdateProductDialog(Product product) {
        AlertDialog.Builder builderAlert = new AlertDialog.Builder(new ContextThemeWrapper(this, androidx.appcompat.R.style.Theme_AppCompat_Dialog_Alert));
        AlertDialog alertDialog = builderAlert.create();
        alertDialog.setTitle("Modifier produit");
        LayoutInflater inflater = getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.add_product_layout, null);
        alertDialog.setView(dialogView);

        EditText productName = dialogView.findViewById(R.id.productName);
        EditText productPrice = dialogView.findViewById(R.id.productPrice);
        EditText productDesc = dialogView.findViewById(R.id.productDesc);

        Button btnAdd = dialogView.findViewById(R.id.btnAdd);
        // change Value of btn
        btnAdd.setText("Modifier");

        Button btnCancel = dialogView.findViewById(R.id.btnCancel);

        // add the old value
        productName.setText(product.getName());
        productPrice.setText(String.valueOf(product.getPrice()));
        productDesc.setText(product.getDescription());

        btnAdd.setOnClickListener(v -> {
            String name = productName.getText().toString();
            String desc = productDesc.getText().toString();
            String price = productPrice.getText().toString();
            if (name.isEmpty() || price.isEmpty() || desc.isEmpty()) {
                Toast.makeText(getApplicationContext(), "Tous les champs sont obligatoire!", Toast.LENGTH_SHORT).show();
                return;
            }
            // update product
            product.setName(name.substring(0, 1).toUpperCase() + name.substring(1).toLowerCase());
            product.setPrice(Double.parseDouble(price));
            product.setDescription(desc);

            boolean isUpdated = db.updateProduct(product);
            if (isUpdated) {
                alertDialog.dismiss();
                Snackbar.make(findViewById(R.id.productLayout), "Le produit '" + name + "' a été modifier avec succès.", Snackbar.LENGTH_LONG).show();
                loadProduct();
            } else {
                Snackbar.make(findViewById(R.id.productLayout), "Échec de modifier, Veuillez réessayer.", Snackbar.LENGTH_LONG).show();
            }
        });

        btnCancel.setOnClickListener(v -> alertDialog.dismiss());

        alertDialog.show();

    }

    // dialog for confirm delete produit
    private void showConfirmDeleteDialog(Product product) {
        AlertDialog.Builder alertBuilder = new AlertDialog.Builder(new ContextThemeWrapper(this, androidx.appcompat.R.style.Theme_AppCompat_Dialog_Alert));

        alertBuilder.setTitle("Confirmation");

        alertBuilder.setMessage("Êtes-vous sûr de vouloir supprimer ce produit ?");

        alertBuilder.setPositiveButton("Supprimer", (dialog, which) -> {
            boolean isDeleted = db.deleteProduct(product.getId());
            if (isDeleted) {
                Snackbar.make(findViewById(R.id.productLayout), "Le produit  a été supprimer avec succès.", Snackbar.LENGTH_LONG).show();
                loadProduct();
            } else {
                Snackbar.make(findViewById(R.id.productLayout), "Échec de suppression, Veuillez réessayer.", Snackbar.LENGTH_LONG).show();
            }
        });

        alertBuilder.setNegativeButton("Annuler", (dialog, which) -> dialog.dismiss());

        alertBuilder.show();
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