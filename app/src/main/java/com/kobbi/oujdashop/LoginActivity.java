package com.kobbi.oujdashop;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.snackbar.Snackbar;
import com.kobbi.oujdashop.Database.Database;
import com.kobbi.oujdashop.Models.User;

import java.util.Objects;

public class LoginActivity extends AppCompatActivity {

    private EditText email, password;
    private Button btnLogin, btnNewAccount;

    private CheckBox rememberMe;

    private SharedPreferences sharedPreferences;

    private Database db;
    private Intent intent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_login);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.loginActivity), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        sharedPreferences = getSharedPreferences("login", MODE_PRIVATE);

        email = findViewById(R.id.email);
        password = findViewById(R.id.password);
        rememberMe = findViewById(R.id.rememberMe);

        btnLogin = findViewById(R.id.btnLogin);
        btnNewAccount = findViewById(R.id.btnNewAccount);

        btnNewAccount.setOnClickListener(v -> {
            Intent intent = new Intent(getApplicationContext(), RegisterActivity.class);
            startActivity(intent);
        });

        db = new Database(getApplicationContext());
        btnLogin.setOnClickListener(v -> {
            String emailValue = email.getText().toString().toLowerCase();
            String passwordValue = password.getText().toString();
            try {
                User user = db.authenticateUser(emailValue, passwordValue);
                if (user != null) {
                    if (rememberMe.isChecked()) {
                        sharedPreferences.edit().putBoolean("isLogin", true).apply();
                    }
                    sharedPreferences.edit().putInt("user_id", user.getId()).apply();
                    Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                    startActivity(intent);
                } else {
                    Snackbar.make(findViewById(R.id.loginActivity), "Email ou mot de passe incorrect.", Snackbar.LENGTH_LONG).show();
                }
            } catch (Exception e) {
                Snackbar.make(findViewById(R.id.loginActivity), Objects.requireNonNull(e.getMessage()), Snackbar.LENGTH_LONG).show();
            }

        });
    }
}