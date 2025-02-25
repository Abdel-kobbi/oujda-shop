package com.kobbi.oujdashop;


import android.os.Bundle;
import android.view.ContextThemeWrapper;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.PopupMenu;
import androidx.appcompat.widget.Toolbar;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.snackbar.Snackbar;
import com.kobbi.oujdashop.Adapters.CategoryAdapter;
import com.kobbi.oujdashop.Models.Category;

import java.util.List;

public class MainActivity extends AppCompatActivity {

    private ListView listViewCategory;

    private List<Category> categoryList;

    private Database db;

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

        // get connection db
        db = new Database(getApplicationContext());

        listViewCategory = findViewById(R.id.listCategory);
        // to display message if the list is empty
        listViewCategory.setEmptyView(findViewById(R.id.emptyListCategory));
        loadCategories();

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
        } else if (item.getItemId() == R.id.profile) {
            // navigate to profile activity
            return true;
        } else {
            return super.onOptionsItemSelected(item);
        }

    }

    private void showAddCategoryDialog() {
        AlertDialog.Builder builderAlert = new AlertDialog.Builder(new ContextThemeWrapper(this, androidx.appcompat.R.style.Theme_AppCompat_Dialog_Alert));
        AlertDialog alertDialog = builderAlert.create();
        alertDialog.setTitle("Ajouter catégorie");
        LayoutInflater inflater = getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.add_category_layout, null);
        alertDialog.setView(dialogView);

        EditText categoryName = dialogView.findViewById(R.id.categoryName);
        EditText categoryDesc = dialogView.findViewById(R.id.categoryDesc);

        Button btnAdd = dialogView.findViewById(R.id.btnAdd);
        Button btnCancel = dialogView.findViewById(R.id.btnCancel);


        btnAdd.setOnClickListener(v -> {
            String name = categoryName.getText().toString();
            String desc = categoryDesc.getText().toString();
            if (name.isEmpty() || desc.isEmpty()) {
                Toast.makeText(getApplicationContext(), "Tous les champs sont obligatoire!", Toast.LENGTH_SHORT).show();
                return;
            }
            boolean isAdded = db.addCategory(new Category(name, desc));
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
}