package com.kobbi.oujdashop;

import android.os.Bundle;
import android.widget.ListView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

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
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // add toolbar to main activity
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // get connection db
        db = new Database(getApplicationContext());

        // initialize the list of categories

        categoryList = db.findAllCategories();


        listViewCategory = findViewById(R.id.listCategory);

        CategoryAdapter adapter = new CategoryAdapter(getApplicationContext(), R.layout.row_category, categoryList);

        // to display message if the list is empty
        listViewCategory.setEmptyView(findViewById(R.id.emptyListCategory));

        listViewCategory.setAdapter(adapter);
    }
}