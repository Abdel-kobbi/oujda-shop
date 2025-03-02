package com.kobbi.oujdashop;

import android.content.Intent;
import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import android.Manifest;
import android.content.pm.PackageManager;
import android.util.Log;
import android.view.ContextThemeWrapper;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageAnalysis;
import androidx.camera.core.ImageProxy;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.camera.view.PreviewView;

import com.google.common.util.concurrent.ListenableFuture;
import com.google.mlkit.vision.barcode.BarcodeScanner;
import com.google.mlkit.vision.barcode.BarcodeScanning;
import com.google.mlkit.vision.barcode.common.Barcode;
import com.google.mlkit.vision.common.InputImage;
import com.kobbi.oujdashop.Database.Database;
import com.kobbi.oujdashop.Models.Category;
import com.kobbi.oujdashop.Models.Product;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ScannerQRActivity extends AppCompatActivity {

    private static final int CAMERA_REQUEST_CODE = 100;
    private BarcodeScanner scanner;
    private PreviewView previewView;
    private ExecutorService cameraExecutor;

    private Database db;

    Category categoryProduct;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_scanner_qractivity);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        db = new Database(getApplicationContext());

        previewView = findViewById(R.id.camera_preview);
        scanner = BarcodeScanning.getClient();
        cameraExecutor = Executors.newSingleThreadExecutor();

        if (ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, CAMERA_REQUEST_CODE);
        } else {
            ShowCamera();
        }

        // get category product
        Category category = (Category) getIntent().getSerializableExtra("category");

        if (category != null) {
            categoryProduct = category;
        } else {
            Intent intent = new Intent(getApplicationContext(), MainActivity.class);
            startActivity(intent);
            finish();
        }
    }

    private void ShowCamera() {
        ListenableFuture<ProcessCameraProvider> cameraProviderFuture =
                ProcessCameraProvider.getInstance(this);

        cameraProviderFuture.addListener(() -> {
            try {
                ProcessCameraProvider cameraProvider = cameraProviderFuture.get();

                CameraSelector cameraSelector = new CameraSelector.Builder()
                        .requireLensFacing(CameraSelector.LENS_FACING_BACK)
                        .build();

                Preview preview = new Preview.Builder().build();
                preview.setSurfaceProvider(previewView.getSurfaceProvider());


                ImageAnalysis imageAnalysis = new ImageAnalysis.Builder()
                        .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                        .build();

                imageAnalysis.setAnalyzer(cameraExecutor, image -> {
                    readImage(image);
                });

                cameraProvider.unbindAll();
                cameraProvider.bindToLifecycle(this, cameraSelector, preview, imageAnalysis);
            } catch (ExecutionException | InterruptedException e) {
                Toast.makeText(getApplicationContext(), "Erreur d'initialisation de la caméra", Toast.LENGTH_SHORT).show();
                Log.e("CameraX", "Erreur lors de l'initialisation de la caméra", e);
            }
        }, ContextCompat.getMainExecutor(this));
    }

    private void readImage(ImageProxy image) {
        try {
            @SuppressWarnings("UnsafeOptInUsageError") // Suppression de l'alerte de compatibilité
            InputImage inputImage = InputImage.fromMediaImage(image.getImage(), image.getImageInfo().getRotationDegrees());

            scanner.process(inputImage)
                    .addOnSuccessListener(barcodes -> {
                        if (!barcodes.isEmpty()) {
                            for (Barcode barcode : barcodes) {
                                String value = barcode.getRawValue();
                                Product product = db.getProductByQRCode(value);
                                if (product != null) {
                                    Intent intent = new Intent(getApplicationContext(), ProductDetailsActivity.class);
                                    intent.putExtra("product", product);
                                    startActivity(intent);
                                    finish();
                                } else {
                                    showAddNewProductDialog(value);
                                }
                                cameraExecutor.shutdown();
                            }
                        }
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(getApplicationContext(), "Erreur de scan", Toast.LENGTH_SHORT).show();
                    })
                    .addOnCompleteListener(task -> image.close()); // Fermer l'image après analyse
        } catch (Exception e) {
            Log.e("BarcodeScanner", "Erreur lors de la conversion de l'image", e);
            image.close();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == CAMERA_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                ShowCamera();
            } else {
                Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                startActivity(intent);
                finish();
                Toast.makeText(getApplicationContext(), "Permission caméra refusée", Toast.LENGTH_LONG).show();
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        cameraExecutor.shutdown();
    }

    private void showAddNewProductDialog(String code) {
        AlertDialog.Builder alertBuilder = new AlertDialog.Builder(new ContextThemeWrapper(this, androidx.appcompat.R.style.Theme_AppCompat_Dialog_Alert));

        alertBuilder.setTitle("Ajouter produit");

        alertBuilder.setMessage("Ce produit n'existe pas, vous devez l'ajouter ?");

        alertBuilder.setPositiveButton("Oui", (dialog, which) -> {
            Intent intent = new Intent(getApplicationContext(), ProductActivity.class);
            intent.putExtra("code", code);
            intent.putExtra("category", categoryProduct);
            startActivity(intent);
            finish();
        });

        alertBuilder.setNegativeButton("Non", (dialog, which) -> {
            dialog.dismiss();
            Intent intent = new Intent(getApplicationContext(), ProductActivity.class);
            intent.putExtra("category", categoryProduct);
            startActivity(intent);
            finish();
        });

        alertBuilder.show();
    }
}
