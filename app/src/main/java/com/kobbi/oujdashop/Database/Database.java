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
    private final String TABLE_FAVORITES = "favorites";


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
                "password TEXT NOT NULL," +
                "image TEXT);";
        db.execSQL(tableUser);
        // table categories
        String tableCategory = "CREATE TABLE IF NOT EXISTS " + TABLE_CATEGORY + " (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "nom TEXT NOT NULL," +
                "description TEXT NOT NULL," +
                "image TEXT);";
        db.execSQL(tableCategory);
        // table products
        String tableProduct = "CREATE TABLE IF NOT EXISTS " + TABLE_PRODUCT + " (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "nom TEXT NOT NULL," +
                "price REAL NOT NULL," +
                "description TEXT NOT NULL," +
                "image TEXT," +
                "qr_code TEXT," +
                "category_id INTEGER," +
                "FOREIGN KEY (category_id) REFERENCES " + TABLE_CATEGORY + "(id));";
        db.execSQL(tableProduct);
        // table favorites
        String tableFavorites = "CREATE TABLE IF NOT EXISTS " + TABLE_FAVORITES + " (" +
                "user_id INTEGER," +
                "product_id INTEGER," +
                "PRIMARY KEY (user_id, product_id)," +
                "FOREIGN KEY (product_id) REFERENCES " + TABLE_PRODUCT + "(id)," +
                "FOREIGN KEY (user_id) REFERENCES " + TABLE_USER + "(id));";
        db.execSQL(tableFavorites);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        String dropUser = "DROP TABLE IF EXISTS " + TABLE_USER;
        db.execSQL(dropUser);
        String dropCategory = "DROP TABLE IF EXISTS " + TABLE_CATEGORY;
        db.execSQL(dropCategory);
        String dropProduct = "DROP TABLE IF EXISTS " + TABLE_PRODUCT;
        db.execSQL(dropProduct);
        String dropFavorites = "DROP TABLE IF EXISTS " + TABLE_FAVORITES;
        db.execSQL(dropFavorites);
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
                    result.getString(4),
                    result.getString(5)
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
                    result.getString(4),
                    result.getString(5)
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
            values.put("image", user.getImage());
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
                        categories.getString(2),
                        categories.getString(3)
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
            values.put("image", category.getImage());
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
            values.put("image", category.getImage());
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

    public List<Product> getProductByCategory(Category category) {
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
                        cursor.getString(4),
                        cursor.getString(5),
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
            values.put("image", product.getImage());
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
            values.put("image", product.getImage());
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

    public Product getProductByQRCode(String qrCode) {
        Product product = null;
        try (SQLiteDatabase db = this.getReadableDatabase()) {
            String sql = "SELECT * FROM " + TABLE_PRODUCT + " WHERE qr_code = ?";
            Cursor cursor = db.rawQuery(sql, new String[]{qrCode});
            while (cursor.moveToNext()) {
                product = new Product(
                        cursor.getInt(0),
                        cursor.getString(1),
                        cursor.getDouble(2),
                        cursor.getString(3),
                        cursor.getString(4),
                        cursor.getString(5),
                        null
                );
            }
            cursor.close();
        } catch (Exception e) {
            Log.d("Error", Objects.requireNonNull(e.getMessage()));
        }
        return product;
    }

    public void addFavorites(int userId, int productId) {
        try (SQLiteDatabase db = this.getWritableDatabase()) {
            ContentValues values = new ContentValues();
            values.put("user_id", userId);
            values.put("product_id", productId);
            db.insertOrThrow(TABLE_FAVORITES, null, values);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void deleteFavorites(int userId, int productId) {
        try (SQLiteDatabase db = this.getWritableDatabase()) {
            db.delete(TABLE_FAVORITES, "user_id = ? AND product_id = ?", new String[]{String.valueOf(userId), String.valueOf(productId)});
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // method to check if the product is favorite to user
    public boolean isFavorite(int userId, int productId) {
        try (SQLiteDatabase db = this.getWritableDatabase()) {
            String sql = "SELECT * FROM " + TABLE_FAVORITES + " WHERE user_id = ? AND product_id = ?;";
            Cursor cursor = db.rawQuery(sql, new String[]{String.valueOf(userId), String.valueOf(productId)});
            boolean result = cursor.getCount() == 1;
            cursor.close();
            return result;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    public List<Product> getAllFavoritesProducts(int userId) {
        List<Product> products = new ArrayList<>();
        try (SQLiteDatabase db = this.getReadableDatabase()) {
            // Récupérer tous les IDs des produits favoris
            String sql = "SELECT product_id FROM " + TABLE_FAVORITES + " WHERE user_id = ?;";
            Cursor cursor = db.rawQuery(sql, new String[]{String.valueOf(userId)});
            List<String> productIds = new ArrayList<>();

            while (cursor.moveToNext()) {
                productIds.add(String.valueOf(cursor.getInt(0)));
            }
            cursor.close();

            // Vérifier s'il y a des favoris
            if (productIds.isEmpty()) {
                return products;
            }

            // Construire la clause "IN (?,?,?)" dynamiquement
            String placeholders = new String(new char[productIds.size()]).replace("\0", "?,").replaceAll(",$", "");
            String sqlProducts = "SELECT * FROM " + TABLE_PRODUCT + " WHERE id IN (" + placeholders + ")";

            Cursor cursorProducts = db.rawQuery(sqlProducts, productIds.toArray(new String[0]));
            while (cursorProducts.moveToNext()) {
                products.add(new Product(
                        cursorProducts.getInt(0),
                        cursorProducts.getString(1),
                        cursorProducts.getDouble(2),
                        cursorProducts.getString(3),
                        cursorProducts.getString(4),
                        cursorProducts.getString(5),
                        null
                ));
            }
            cursorProducts.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return products;
    }
}
