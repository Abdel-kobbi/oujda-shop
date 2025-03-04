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
import android.widget.ImageButton;
import android.widget.ListView;
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
import com.kobbi.oujdashop.Adapters.CategoryAdapter;
import com.kobbi.oujdashop.Database.Database;
import com.kobbi.oujdashop.Models.Category;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.Objects;

public class MainActivity extends AppCompatActivity {

    private ListView listViewCategory;

    private List<Category> categoryList;

    private Database db;

    private Uri pathImage = null;

    private ImageButton categoryImg;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.categoryLayout), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // add toolbar to main activity
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        Objects.requireNonNull(getSupportActionBar()).setTitle("Liste des categories");

        // get connection db
        db = new Database(getApplicationContext());

        listViewCategory = findViewById(R.id.listCategory);

        registerForContextMenu(listViewCategory);

        // to display message if the list is empty
        listViewCategory.setEmptyView(findViewById(R.id.emptyListCategory));
        loadCategories();

        listViewCategory.setOnItemClickListener((parent, view, position, id) -> {
            Category category = categoryList.get(position);
            Intent intent = new Intent(getApplicationContext(), ProductActivity.class);
            intent.putExtra("category", category);
            startActivity(intent);
        });
    }

    private void loadCategories() {
        categoryList = db.findAllCategories();
        CategoryAdapter adapter = new CategoryAdapter(getApplicationContext(), R.layout.row_category, categoryList);
        listViewCategory.setAdapter(adapter);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.add) {
            showAddCategoryDialog();
            return true;
        }else if (item.getItemId() == R.id.profile) {
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

    // context menu for update or delete category
    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        getMenuInflater().inflate(R.menu.context_menu, menu);
    }

    @Override
    public boolean onContextItemSelected(@NonNull MenuItem item) {
        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
        Category category = categoryList.get(info.position);
        int itemSelected = item.getItemId();
        if (itemSelected == R.id.update) {
            showUpdateCategoryDialog(category);
            return true;
        } else if (itemSelected == R.id.delete) {
            showConfirmDeleteDialog(category);
            return true;
        } else {
            return super.onContextItemSelected(item);
        }
    }

    // dialog for add new category
    private void showAddCategoryDialog() {
        AlertDialog.Builder builderAlert = new AlertDialog.Builder(new ContextThemeWrapper(this, androidx.appcompat.R.style.Theme_AppCompat_Dialog_Alert));
        AlertDialog alertDialog = builderAlert.create();
        alertDialog.setTitle("Ajouter catégorie");
        LayoutInflater inflater = getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.add_category_layout, null);
        alertDialog.setView(dialogView);

        EditText categoryName = dialogView.findViewById(R.id.categoryName);
        EditText categoryDesc = dialogView.findViewById(R.id.categoryDesc);
        categoryImg = dialogView.findViewById(R.id.categoryImg);

        Button btnAdd = dialogView.findViewById(R.id.btnAdd);
        Button btnCancel = dialogView.findViewById(R.id.btnCancel);

        categoryImg.setOnClickListener(v -> openGallery());

        btnAdd.setOnClickListener(v -> {
            String name = categoryName.getText().toString();
            String desc = categoryDesc.getText().toString();
            if (name.isEmpty() || desc.isEmpty()) {
                Toast.makeText(getApplicationContext(), "Tous les champs sont obligatoire!", Toast.LENGTH_SHORT).show();
                return;
            }

            // get image from ImageButton and store it
            Drawable drawable = categoryImg.getDrawable();
            Bitmap bitmap = null;

            if (drawable instanceof BitmapDrawable) {
                bitmap = ((BitmapDrawable) drawable).getBitmap();
            }

            String path = "";
            if (bitmap != null) {
                path = saveImageToInternalStorage(bitmap, String.valueOf(new Date().getTime()).substring(5));
            }

            boolean isAdded = db.addCategory(new Category(name.substring(0, 1).toUpperCase() + name.substring(1).toLowerCase(), desc, path));
            if (isAdded) {
                alertDialog.dismiss();
                Snackbar.make(findViewById(R.id.categoryLayout), "La catégorie '" + name + "' a été ajoutée avec succès.", Snackbar.LENGTH_LONG).show();
                loadCategories();
            } else {
                Snackbar.make(findViewById(R.id.categoryLayout), "Échec de l'ajout, Veuillez réessayer.", Snackbar.LENGTH_LONG).show();
            }
        });

        btnCancel.setOnClickListener(v -> {
            alertDialog.dismiss();
        });

        alertDialog.show();
    }

    // dialog for update category;
    private void showUpdateCategoryDialog(Category category) {
        AlertDialog.Builder builderAlert = new AlertDialog.Builder(new ContextThemeWrapper(this, androidx.appcompat.R.style.Theme_AppCompat_Dialog_Alert));
        AlertDialog alertDialog = builderAlert.create();
        alertDialog.setTitle("Modifier catégorie");
        LayoutInflater inflater = getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.add_category_layout, null);
        alertDialog.setView(dialogView);

        EditText categoryName = dialogView.findViewById(R.id.categoryName);
        EditText categoryDesc = dialogView.findViewById(R.id.categoryDesc);
        categoryImg = dialogView.findViewById(R.id.categoryImg);

        Button btnAdd = dialogView.findViewById(R.id.btnAdd);
        // change Value of btn
        btnAdd.setText("Modifier");

        Button btnCancel = dialogView.findViewById(R.id.btnCancel);

        categoryImg.setOnClickListener(v -> openGallery());

        // add the old value
        categoryName.setText(category.getName());
        categoryDesc.setText(category.getDescription());
        Bitmap bitmapCategory = loadImageFromStorage(category.getImage());
        if (bitmapCategory != null) {
            categoryImg.setImageBitmap(bitmapCategory);
        }

        btnAdd.setOnClickListener(v -> {
            String name = categoryName.getText().toString().trim();
            String desc = categoryDesc.getText().toString().trim();
            if (name.isEmpty() || desc.isEmpty()) {
                Toast.makeText(getApplicationContext(), "Tous les champs sont obligatoire!", Toast.LENGTH_SHORT).show();
                return;
            }
            // get image from ImageButton and store it
            Drawable drawable = categoryImg.getDrawable();
            Bitmap bitmap = null;

            if (drawable instanceof BitmapDrawable) {
                bitmap = ((BitmapDrawable) drawable).getBitmap();
            }

            String path = "";
            if (bitmap != null) {
                path = saveImageToInternalStorage(bitmap, String.valueOf(new Date().getTime()).substring(5));
            }

            // update category
            category.setName(name.substring(0, 1).toUpperCase() + name.substring(1).toLowerCase());
            category.setDescription(desc);
            category.setImage(path);

            boolean isUpdated = db.updateCategory(category);
            if (isUpdated) {
                alertDialog.dismiss();
                Snackbar.make(findViewById(R.id.categoryLayout), "La catégorie '" + name + "' a été modifier avec succès.", Snackbar.LENGTH_LONG).show();
                loadCategories();
            } else {
                Snackbar.make(findViewById(R.id.categoryLayout), "Échec de modifier, Veuillez réessayer.", Snackbar.LENGTH_LONG).show();
            }
        });

        btnCancel.setOnClickListener(v -> {
            alertDialog.dismiss();
        });

        alertDialog.show();

    }

    // dialog for confirm delete category
    private void showConfirmDeleteDialog(Category category) {
        AlertDialog.Builder alertBuilder = new AlertDialog.Builder(new ContextThemeWrapper(this, androidx.appcompat.R.style.Theme_AppCompat_Dialog_Alert));

        alertBuilder.setTitle("Confirmation");

        alertBuilder.setMessage("Êtes-vous sûr de vouloir supprimer cette catégorie ?");

        alertBuilder.setPositiveButton("Supprimer", (dialog, which) -> {
            boolean isDeleted = db.deleteCategory(category.getId());
            if (isDeleted) {
                Snackbar.make(findViewById(R.id.categoryLayout), "La catégorie  a été supprimer avec succès.", Snackbar.LENGTH_LONG).show();
                loadCategories();
            } else {
                Snackbar.make(findViewById(R.id.categoryLayout), "Échec de suppression, Veuillez réessayer.", Snackbar.LENGTH_LONG).show();
            }
        });

        alertBuilder.setNegativeButton("Annuler", (dialog, which) -> dialog.dismiss());

        alertBuilder.show();
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
            categoryImg.setImageURI(pathImage);
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