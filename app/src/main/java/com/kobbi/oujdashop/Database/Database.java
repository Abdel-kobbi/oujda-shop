package com.kobbi.oujdashop.Database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteConstraintException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import androidx.annotation.Nullable;

import com.kobbi.oujdashop.Models.Category;
import com.kobbi.oujdashop.Models.Product;
import com.kobbi.oujdashop.Models.User;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class Database extends SQLiteOpenHelper {

    private final String TABLE_USER = "users";
    private final String TABLE_CATEGORY = "categories";
    private final String TABLE_PRODUCT = "products";


    public Database(@Nullable Context context) {
        super(context, "OujdaShopDB.db", null, 1);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // enable foreign key
        db.execSQL("PRAGMA foreign_keys = ON;");

        // table users
        String tableUser = "CREATE TABLE IF NOT EXISTS " + TABLE_USER + " (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "nom TEXT NOT NULL," +
                "prenom TEXT NOT NULL," +
                "email TEXT NOT NULL UNIQUE," +
                "password TEXT NOT NULL);";
        db.execSQL(tableUser);
        // table categories
        String tableCategory = "CREATE TABLE IF NOT EXISTS " + TABLE_CATEGORY + " (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "nom TEXT NOT NULL," +
                "description TEXT NOT NULL);";
        db.execSQL(tableCategory);
        // table products
        String tableProduct = "CREATE TABLE IF NOT EXISTS " + TABLE_PRODUCT + " (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "nom TEXT NOT NULL," +
                "price REAL NOT NULL," +
                "description TEXT NOT NULL," +
                "category_id INTEGER," +
                "FOREIGN KEY (category_id) REFERENCES " + TABLE_CATEGORY + "(id));";
        db.execSQL(tableProduct);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        String dropUser = "DROP TABLE IF EXISTS " + TABLE_USER;
        db.execSQL(dropUser);
        String dropCategory = "DROP TABLE IF EXISTS " + TABLE_CATEGORY;
        db.execSQL(dropCategory);
        String dropProduct = "DROP TABLE IF EXISTS " + TABLE_PRODUCT;
        db.execSQL(dropProduct);
        onCreate(db);
    }

    // method to add new user

    /**
     * return true if the user is added
     * return false if email already exists
     */
    public boolean addUser(User user) {
        try (SQLiteDatabase db = this.getWritableDatabase()) {

            ContentValues values = new ContentValues();
            values.put("nom", user.getNom());
            values.put("prenom", user.getPrenom());
            values.put("email", user.getEmail());
            values.put("password", user.getPassword());
            db.insertOrThrow(TABLE_USER, null, values);
            return true;
        } catch (SQLiteConstraintException e) {
            return false;
        }
    }

    // method check if the user in the db
    public User authenticateUser(String email, String password) {
        User user = null;
        SQLiteDatabase db = this.getReadableDatabase();
        String sql = "SELECT * FROM " + TABLE_USER + " WHERE email = ? AND password = ?;";
        Cursor result = db.rawQuery(sql, new String[]{email, password});
        if (result.moveToFirst()) {
            user = new User(result.getInt(0),
                    result.getString(1),
                    result.getString(2),
                    result.getString(3),
                    result.getString(4)
            );
        }
        result.close();
        db.close();
        return user;
    }

    // method to find user
    public User findUser(int id) {
        User user = null;
        SQLiteDatabase db = this.getReadableDatabase();
        String sql = "SELECT * FROM " + TABLE_USER + " WHERE id = ?;";
        Cursor result = db.rawQuery(sql, new String[]{String.valueOf(id)});
        if (result.moveToFirst()) {
            user = new User(result.getInt(0),
                    result.getString(1),
                    result.getString(2),
                    result.getString(3),
                    result.getString(4)
            );
        }
        result.close();
        db.close();
        return user;
    }

    public boolean updateUser(User user) {
        try (SQLiteDatabase db = this.getWritableDatabase()) {
            ContentValues values = new ContentValues();
            values.put("nom", user.getNom());
            values.put("prenom", user.getPrenom());
            values.put("password", user.getPassword());
            db.update(TABLE_USER, values, "id = ?", new String[]{String.valueOf(user.getId())});
            return true;
        } catch (SQLiteConstraintException e) {
            return false;
        }
    }

    // find all categories
    public List<Category> findAllCategories() {
        List<Category> listCategories = new ArrayList<>();
        try (SQLiteDatabase db = this.getReadableDatabase()) {
            String sql = "SELECT * FROM " + TABLE_CATEGORY;
            Cursor categories = db.rawQuery(sql, null);
            while (categories.moveToNext()) {
                listCategories.add(new Category(
                        categories.getInt(0),
                        categories.getString(1),
                        categories.getString(2)
                ));
            }
            categories.close();
        } catch (Exception e) {
            Log.d("Error", Objects.requireNonNull(e.getMessage()));
        }
        return listCategories;
    }

    public boolean addCategory(Category category) {
        try (SQLiteDatabase db = this.getWritableDatabase()) {

            ContentValues values = new ContentValues();
            values.put("nom", category.getName());
            values.put("description", category.getDescription());
            db.insertOrThrow(TABLE_CATEGORY, null, values);
            return true;
        } catch (SQLiteConstraintException e) {
            return false;
        }
    }

    public boolean updateCategory(Category category) {
        try (SQLiteDatabase db = this.getWritableDatabase()) {
            ContentValues values = new ContentValues();
            values.put("nom", category.getName());
            values.put("description", category.getDescription());
            db.update(TABLE_CATEGORY, values, "id = ?", new String[]{String.valueOf(category.getId())});
            return true;
        } catch (SQLiteConstraintException e) {
            return false;
        }
    }

    public boolean deleteCategory(int id) {
        try (SQLiteDatabase db = this.getWritableDatabase()) {
            db.delete(TABLE_CATEGORY, "id = ?", new String[]{String.valueOf(id)});
            return true;
        } catch (SQLiteConstraintException e) {
            return false;
        }
    }

    public List<Product> getProductBuCategory(Category category) {
        List<Product> products = new ArrayList<>();
        try (SQLiteDatabase db = this.getReadableDatabase()) {
            String sql = "SELECT * FROM " + TABLE_PRODUCT + " WHERE category_id = ?";
            Cursor cursor = db.rawQuery(sql, new String[]{String.valueOf(category.getId())});
            while (cursor.moveToNext()) {
                products.add(new Product(
                        cursor.getInt(0),
                        cursor.getString(1),
                        cursor.getDouble(2),
                        cursor.getString(3),
                        category
                ));
            }
            cursor.close();
        } catch (Exception e) {
            Log.d("Error", Objects.requireNonNull(e.getMessage()));
        }
        return products;
    }

    public boolean addProduct(Product product) {
        try (SQLiteDatabase db = this.getWritableDatabase()) {
            ContentValues values = new ContentValues();
            values.put("nom", product.getName());
            values.put("price", product.getPrice());
            values.put("description", product.getDescription());
            values.put("category_id", product.getCategory().getId());
            db.insertOrThrow(TABLE_PRODUCT, null, values);
            return true;
        } catch (SQLiteConstraintException e) {
            return false;
        }
    }

    public boolean updateProduct(Product product) {
        try (SQLiteDatabase db = this.getWritableDatabase()) {
            ContentValues values = new ContentValues();
            values.put("nom", product.getName());
            values.put("price", product.getPrice());
            values.put("description", product.getDescription());
            db.update(TABLE_PRODUCT, values, "id = ?", new String[]{String.valueOf(product.getId())});
            return true;
        } catch (SQLiteConstraintException e) {
            return false;
        }
    }

    public boolean deleteProduct(int id) {
        try (SQLiteDatabase db = this.getWritableDatabase()) {
            db.delete(TABLE_PRODUCT, "id = ?", new String[]{String.valueOf(id)});
            return true;
        } catch (SQLiteConstraintException e) {
            return false;
        }
    }
}
