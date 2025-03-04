package com.kobbi.oujdashop;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
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
import android.widget.ImageButton;
import android.widget.TextView;
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

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.Objects;

public class ProductActivity extends AppCompatActivity {

    private GridView gridViewProduct;

    private Database db;

    private Category categoryProduct;

    private List<Product> products;

    private Uri pathImage = null;

    private ImageButton productImg;

    String codeScanner; // store value of code

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

        Intent intent = getIntent();
        // get category product
        Category category = (Category) intent.getSerializableExtra("category");

        if (category != null) {
            categoryProduct = category;
            Objects.requireNonNull(getSupportActionBar()).setTitle("Liste des produits-" + category.getName());
        } else {
            Intent intentGoToMain = new Intent(getApplicationContext(), MainActivity.class);
            startActivity(intentGoToMain);
            finish();
        }

        // check if add new product from scanner
        codeScanner = intent.getStringExtra("code");
        if (codeScanner != null) {
            showAddProductDialog();
        }

        loadProduct();

        gridViewProduct.setOnItemClickListener((parent, view, position, id) -> {
            Product selectedProduct = products.get(position);
            Intent intentGoToProductDetails = new Intent(getApplicationContext(), ProductDetailsActivity.class);
            intentGoToProductDetails.putExtra("product", selectedProduct);
            startActivity(intentGoToProductDetails);
        });

        // add context menu to grid view
        registerForContextMenu(gridViewProduct);

    }

    private void loadProduct() {
        products = db.getProductByCategory(categoryProduct);
        ProductAdapter adapter = new ProductAdapter(getApplicationContext(), R.layout.item_product, products);
        gridViewProduct.setAdapter(adapter);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.product_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.add) {
            // show dialog to add new product
            showAddProductDialog();
            return true;
        } else if (item.getItemId() == R.id.scanner) {
            Intent intent = new Intent(getApplicationContext(), ScannerQRActivity.class);
            intent.putExtra("category", categoryProduct);
            startActivity(intent);
            return true;
        } else if (item.getItemId() == R.id.favorites) {
            // navigate to favorites activity
            Intent intent = new Intent(getApplicationContext(), FavoritesActivity.class);
            startActivity(intent);
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
        productImg = dialogView.findViewById(R.id.productImg);
        // add the code if is scanner from Scanner activity
        if (codeScanner != null) {
            TextView productCodeScanner = dialogView.findViewById(R.id.codeScanner);
            productCodeScanner.setText(codeScanner);
        }

        Button btnAdd = dialogView.findViewById(R.id.btnAdd);
        Button btnCancel = dialogView.findViewById(R.id.btnCancel);

        productImg.setOnClickListener(v -> openGallery());


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

            // get image from ImageButton and store it
            Drawable drawable = productImg.getDrawable();
            Bitmap bitmap = null;

            if (drawable instanceof BitmapDrawable) {
                bitmap = ((BitmapDrawable) drawable).getBitmap();
            }

            String path = "";
            if (bitmap != null) {
                path = saveImageToInternalStorage(bitmap, String.valueOf(new Date().getTime()).substring(5));
            }

            boolean isAdded = db.addProduct(new Product(name.substring(0, 1).toUpperCase() + name.substring(1).toLowerCase(), Double.parseDouble(price), desc, path, codeScanner, categoryProduct));
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
        productImg = dialogView.findViewById(R.id.productImg);

        Button btnAdd = dialogView.findViewById(R.id.btnAdd);
        // change Value of btn
        btnAdd.setText("Modifier");

        Button btnCancel = dialogView.findViewById(R.id.btnCancel);

        productImg.setOnClickListener(v -> openGallery());

        // add the old value
        productName.setText(product.getName());
        productPrice.setText(String.valueOf(product.getPrice()));
        productDesc.setText(product.getDescription());

        Bitmap bitmapCategory = loadImageFromStorage(product.getImage());
        if (bitmapCategory != null) {
            productImg.setImageBitmap(bitmapCategory);
        }

        btnAdd.setOnClickListener(v -> {
            String name = productName.getText().toString();
            String desc = productDesc.getText().toString();
            String price = productPrice.getText().toString();
            if (name.isEmpty() || price.isEmpty() || desc.isEmpty()) {
                Toast.makeText(getApplicationContext(), "Tous les champs sont obligatoire!", Toast.LENGTH_SHORT).show();
                return;
            }

            // get image from ImageButton and store it
            Drawable drawable = productImg.getDrawable();
            Bitmap bitmap = null;

            if (drawable instanceof BitmapDrawable) {
                bitmap = ((BitmapDrawable) drawable).getBitmap();
            }

            String path = "";
            if (bitmap != null) {
                path = saveImageToInternalStorage(bitmap, String.valueOf(new Date().getTime()).substring(5));
            }

            // update product
            product.setName(name.substring(0, 1).toUpperCase() + name.substring(1).toLowerCase());
            product.setPrice(Double.parseDouble(price));
            product.setDescription(desc);
            product.setImage(path);

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

    // to add img
    private void openGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        intent.setType("image/*"); // select just photos
        startActivityForResult(intent, 100);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 100 && resultCode == RESULT_OK && data != null) {
            pathImage = data.getData();
            productImg.setImageURI(pathImage);
        }
    }

    public String saveImageToInternalStorage(Bitmap bitmap, String imageName) {
        File directory = getApplicationContext().getFilesDir(); // Répertoire interne
        File file = new File(directory, imageName + ".jpg");

        try (FileOutputStream fos = new FileOutputStream(file)) {
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos); // Compression en JPEG
            return file.getAbsolutePath(); // Retourne le chemin de l'image
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    public Bitmap loadImageFromStorage(String path) {
        return BitmapFactory.decodeFile(path);
    }

}