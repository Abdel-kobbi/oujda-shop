package com.kobbi.oujdashop;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteConstraintException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import androidx.annotation.Nullable;

import com.kobbi.oujdashop.Models.Category;
import com.kobbi.oujdashop.Models.User;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class Database extends SQLiteOpenHelper {

    private final String TABLE_USER = "users";
    private final String TABLE_CATEGORY = "categories";


    public Database(@Nullable Context context) {
        super(context, "OujdaShopDB.db", null, 1);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
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
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        String dropUser = "DROP TABLE IF EXISTS " + TABLE_USER;
        db.execSQL(dropUser);
        String dropCategory = "DROP TABLE IF EXISTS " + TABLE_CATEGORY;
        db.execSQL(dropCategory);
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


}
