package com.kobbi.oujdashop;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.snackbar.Snackbar;
import com.kobbi.oujdashop.Models.User;

import java.util.Objects;

public class RegisterActivity extends AppCompatActivity {

    private Button btnLogin, btnSingUp;
    private EditText nom, prenom, email, password, confirmPassword;

    private Database db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_register);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.registerLayout), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        // btn back to login activity
        btnLogin = findViewById(R.id.btnLogin);
        // btn register new account
        btnSingUp = findViewById(R.id.btnSingUp);
        // user info
        nom = findViewById(R.id.nom);
        prenom = findViewById(R.id.prenom);
        email = findViewById(R.id.email);
        password = findViewById(R.id.password);
        confirmPassword = findViewById(R.id.confirmPassword);

        // database
        db = new Database(getApplicationContext());

        btnLogin.setOnClickListener(this::backToLogin);

        btnSingUp.setOnClickListener(this::singUp);
    }

    // function for validate editText and register new user
    private void singUp(View v) {
        String nomValue = nom.getText().toString();
        String prenomValue = prenom.getText().toString();
        String emailValue = email.getText().toString();
        String passwordValue = password.getText().toString();
        String confirmPasswordValue = confirmPassword.getText().toString();

        try {
            if (nomValue.trim().isEmpty()) {
                throw new Exception("Veuillez entrer votre nom.");
            }

            if (prenomValue.trim().isEmpty()) {
                throw new Exception("Veuillez entrer votre prénom.");
            }

            if (emailValue.trim().isEmpty()) {
                throw new Exception("Veuillez entrer votre email.");
            }

            if (passwordValue.isEmpty()) {
                throw new Exception("Veuillez entrer votre mot de passe.");
            }

            if (confirmPasswordValue.isEmpty()) {
                throw new Exception("Veuillez confirmer votre mot de passe.");
            }

            if (!passwordValue.equals(confirmPasswordValue)) {
                throw new Exception("La confirmation du mot de passe est incorrecte.");
            }
            User user = new User(nomValue.trim(), prenomValue.trim(), emailValue.trim().toLowerCase(), passwordValue);

            boolean isSaved = db.addUser(user);

            if (isSaved) {
                Snackbar.make(findViewById(R.id.registerLayout), "Compte créé avec succès.", Snackbar.LENGTH_LONG)
                        .setAction("Se connecter", this::backToLogin).show();
                emptyForm();
            } else {
                Snackbar.make(findViewById(R.id.registerLayout), "Cet email est déjà enregistré. Veuillez utiliser un email différent.", Snackbar.LENGTH_LONG).show();
            }
        } catch (Exception e) {
            Snackbar.make(findViewById(R.id.registerLayout), Objects.requireNonNull(e.getMessage()), Snackbar.LENGTH_LONG).show();
        }
    }

    private void backToLogin(View v) {
        Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
        startActivity(intent);
    }

    private void emptyForm() {
        nom.setText("");
        prenom.setText("");
        email.setText("");
        password.setText("");
        confirmPassword.setText("");
    }
}