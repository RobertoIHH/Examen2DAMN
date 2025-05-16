package com.escom.UPush;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.widget.EditText;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.messaging.FirebaseMessaging;

public class LoginActivity extends AppCompatActivity {
    private FirebaseAuth mAuth;
    private EditText emailEditText, passwordEditText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            requestNotificationPermission();
        }

        mAuth = FirebaseAuth.getInstance();
        emailEditText = findViewById(R.id.email_edit_text);
        passwordEditText = findViewById(R.id.password_edit_text);

        findViewById(R.id.login_button).setOnClickListener(v -> loginUser());
        findViewById(R.id.register_button).setOnClickListener(v -> navigateToRegister());
        findViewById(R.id.admin_login_button).setOnClickListener(v -> navigateToAdminLogin());
    }
    private void requestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) !=
                    PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.POST_NOTIFICATIONS},
                        100);
            }
        }
    }

    private void loginUser() {
        String email = emailEditText.getText().toString();
        String password = passwordEditText.getText().toString();

        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        checkUserRole();
                    } else {
                        Toast.makeText(LoginActivity.this, "Autenticación fallida",
                                Toast.LENGTH_SHORT).show();
                    }
                });
    }


    private void navigateToRegister() {
        startActivity(new Intent(LoginActivity.this, RegisterActivity.class));
    }

    private void navigateToAdminLogin() {
        startActivity(new Intent(LoginActivity.this, AdminRegisterActivity.class));
    }

    private void checkUserRole() {
        String userId = mAuth.getCurrentUser().getUid();
        DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("users").child(userId);

        userRef.child("role").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    String role = snapshot.getValue(String.class);

                    // Solicitar y actualizar el token FCM después de una autenticación exitosa
                    updateFCMToken();

                    if ("admin".equals(role)) {
                        startActivity(new Intent(LoginActivity.this, AdminActivity.class));
                    } else {
                        startActivity(new Intent(LoginActivity.this, UserActivity.class));
                    }
                    finish();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(LoginActivity.this, "Error al verificar rol",
                        Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateFCMToken() {
        FirebaseMessaging.getInstance().getToken()
                .addOnCompleteListener(task -> {
                    if (!task.isSuccessful()) {
                        Log.w("LoginActivity", "Error al obtener token FCM", task.getException());
                        return;
                    }

                    String token = task.getResult();
                    String userId = mAuth.getCurrentUser().getUid();
                    DatabaseReference tokenRef = FirebaseDatabase.getInstance().getReference("tokens")
                            .child(userId);
                    tokenRef.setValue(token);
                });
    }
    private void subscribeToNotificationsTopic() {
        FirebaseMessaging.getInstance().subscribeToTopic("notifications")
                .addOnCompleteListener(task -> {
                    if (!task.isSuccessful()) {
                        Log.w("LoginActivity", "Error al suscribirse a notificaciones", task.getException());
                        return;
                    }
                    Log.d("LoginActivity", "Suscrito al tema de notificaciones");
                });
    }

}