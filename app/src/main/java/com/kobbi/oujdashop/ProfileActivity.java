package com.kobbi.oujdashop;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.ContextThemeWrapper;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.snackbar.Snackbar;
import com.kobbi.oujdashop.Database.Database;
import com.kobbi.oujdashop.Models.User;

import java.util.Objects;

public class ProfileActivity extends AppCompatActivity {

    private TextView fullName;
    private EditText nom, prenom, email;
    private Button btnUpdate, btnChangePassword;

    private Database db;

    User user;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_profile);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.profileLayout), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        // add toolbar to profile activity
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        db = new Database(getApplicationContext());

        fullName = findViewById(R.id.fullName);
        nom = findViewById(R.id.nom);
        prenom = findViewById(R.id.prenom);
        email = findViewById(R.id.email);

        btnUpdate = findViewById(R.id.btnUpdate);
        btnChangePassword = findViewById(R.id.btnChangePassword);

        SharedPreferences sharedPreferences = getSharedPreferences("login", MODE_PRIVATE);

        int idUser = sharedPreferences.getInt("id", 0);
        user = db.findUser(idUser);

        if (idUser == 0 || user == null) {
            Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
            startActivity(intent);
            finish();
        }
        // set inputs with user info

        String userFullName = user.getNom().toUpperCase() + " " + user.getPrenom();
        fullName.setText(userFullName);

        nom.setText(user.getNom());
        prenom.setText(user.getPrenom());
        email.setText(user.getEmail());

        btnUpdate.setOnClickListener(this::updateUser);
        btnChangePassword.setOnClickListener(this::updatePassword);
    }


    private void updateUser(View view) {
        String nomValue = nom.getText().toString().trim();
        String prenomValue = prenom.getText().toString().trim();
        try {
            if (nomValue.isEmpty()) {
                throw new Exception("Veuillez entrer votre nom.");
            }

            if (prenomValue.isEmpty()) {
                throw new Exception("Veuillez entrer votre prénom.");
            }

            user.setNom(nomValue);
            user.setPrenom(prenomValue);

            boolean isUpdated = db.updateUser(user);
            if (isUpdated) {
                String userFullName = user.getNom().toUpperCase() + " " + user.getPrenom();
                fullName.setText(userFullName);
                Snackbar.make(findViewById(R.id.profileLayout), "Votre profile a été modifier avec succès.", Snackbar.LENGTH_LONG).show();
            } else {
                Snackbar.make(findViewById(R.id.profileLayout), "Échec de modifier, Veuillez réessayer.", Snackbar.LENGTH_LONG).show();
            }
        } catch (Exception e) {
            Snackbar.make(findViewById(R.id.profileLayout), Objects.requireNonNull(e.getMessage()), Snackbar.LENGTH_LONG).show();
        }
    }

    private void updatePassword(View view) {
        AlertDialog.Builder builderAlert = new AlertDialog.Builder(new ContextThemeWrapper(this, androidx.appcompat.R.style.Theme_AppCompat_Dialog_Alert));
        AlertDialog alertDialog = builderAlert.create();
        alertDialog.setTitle("Modifier mot de passe");
        LayoutInflater inflater = getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.update_user_password, null);
        alertDialog.setView(dialogView);

        EditText currentPassword = dialogView.findViewById(R.id.currentPassword);
        EditText newPassword = dialogView.findViewById(R.id.newPassword);
        EditText confirmNewPassword = dialogView.findViewById(R.id.confirmNewPassword);

        Button btnAdd = dialogView.findViewById(R.id.btnUpdate);
        Button btnCancel = dialogView.findViewById(R.id.btnCancel);


        btnAdd.setOnClickListener(v -> {
            String currentPasswordValue = currentPassword.getText().toString();
            String newPasswordValue = newPassword.getText().toString();
            String confirmNewPasswordValue = confirmNewPassword.getText().toString();

            try {
                if (currentPasswordValue.isEmpty()) {
                    throw new Exception("Veuillez entrer votre mot de passe actuel.");
                }
                if (newPasswordValue.isEmpty()) {
                    throw new Exception("Veuillez entrer un nouveau mot de passe.");
                }
                if (confirmNewPasswordValue.isEmpty()) {
                    throw new Exception("Veuillez entrer la confirmation de nouveau mot de passe.");
                }

                if (!newPasswordValue.equals(confirmNewPasswordValue)) {
                    throw new Exception("La confirmation du mot de passe est incorrecte.");
                }

                if (!currentPasswordValue.equals(user.getPassword())) {
                    throw new Exception("Votre mot de passe est incorrect. Veuillez réessayer.");
                }

                user.setPassword(newPasswordValue);
                boolean isUpdated = db.updateUser(user);
                alertDialog.dismiss();
                if (isUpdated) {
                    Snackbar.make(findViewById(R.id.profileLayout), "Votre mot de passe a été modifié avec succès.", Snackbar.LENGTH_LONG).show();
                } else {
                    Snackbar.make(findViewById(R.id.profileLayout), "Échec de modifier, Veuillez réessayer.", Snackbar.LENGTH_LONG).show();
                }
            } catch (Exception e) {
                alertDialog.dismiss();
                Snackbar.make(findViewById(R.id.profileLayout), Objects.requireNonNull(e.getMessage()), Snackbar.LENGTH_LONG).show();
            }

        });

        btnCancel.setOnClickListener(v -> {
            alertDialog.dismiss();
        });

        alertDialog.show();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.profile_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        Intent intent;
        if (item.getItemId() == R.id.listCategories) {
            // navigate to category activity
            intent = new Intent(getApplicationContext(), MainActivity.class);
            startActivity(intent);
            return true;
        } else if (item.getItemId() == R.id.logout) {
            // logout and navigate to login activity
            SharedPreferences sharedPreferences = getSharedPreferences("isLogin", MODE_PRIVATE);
            sharedPreferences.edit().clear().apply();
            intent = new Intent(getApplicationContext(), LoginActivity.class);
            startActivity(intent);
            finish();
            return true;
        } else {
            return super.onOptionsItemSelected(item);
        }
    }

}