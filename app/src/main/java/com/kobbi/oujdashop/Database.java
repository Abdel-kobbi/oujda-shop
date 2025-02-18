package com.kobbi.oujdashop;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteConstraintException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import androidx.annotation.Nullable;

import com.kobbi.oujdashop.Models.User;

public class Database extends SQLiteOpenHelper {

    private final String TABLE_USER = "users";


    public Database(@Nullable Context context) {
        super(context, "OujdaShopDB.db", null, 1);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String sql = "CREATE TABLE IF NOT EXISTS " + TABLE_USER + " (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "nom TEXT NOT NULL," +
                "prenom TEXT NOT NULL," +
                "email TEXT NOT NULL UNIQUE," +
                "password TEXT NOT NULL);";
        db.execSQL(sql);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        String sql = "DROP TABLE IF EXISTS " + TABLE_USER;
        db.execSQL(sql);
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


}
