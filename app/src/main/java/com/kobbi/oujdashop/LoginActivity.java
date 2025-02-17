package com.kobbi.oujdashop;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.kobbi.oujdashop.Models.User;

public class LoginActivity extends AppCompatActivity {

    private EditText email, password;
    private Button btnLogin, btnNewAccount;

    private Database db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_login);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        email = findViewById(R.id.email);
        password = findViewById(R.id.password);

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
                    Toast.makeText(getApplicationContext(), user.getNom(), Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(getApplicationContext(), "user not found", Toast.LENGTH_LONG).show();
                }
            } catch (Exception e) {
                throw new RuntimeException(e);
            }

        });
    }
}