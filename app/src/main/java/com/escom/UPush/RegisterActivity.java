package com.escom.UPush;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.messaging.FirebaseMessaging;

import java.util.HashMap;
import java.util.Map;

public class RegisterActivity extends AppCompatActivity {
    private FirebaseAuth mAuth;
    private EditText emailEditText, passwordEditText, nameEditText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        mAuth = FirebaseAuth.getInstance();
        emailEditText = findViewById(R.id.email_edit_text);
        passwordEditText = findViewById(R.id.password_edit_text);
        nameEditText = findViewById(R.id.name_edit_text);

        findViewById(R.id.register_button).setOnClickListener(v -> registerUser());
    }

    private void registerUser() {
        String email = emailEditText.getText().toString();
        String password = passwordEditText.getText().toString();
        String name = nameEditText.getText().toString();

        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        saveUserData(name, "normal");
                    } else {
                        Toast.makeText(RegisterActivity.this, "Registro fallido",
                                Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void saveUserData(String name, String role) {
        String userId = mAuth.getCurrentUser().getUid();
        DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("users").child(userId);

        Map<String, Object> userData = new HashMap<>();
        userData.put("name", name);
        userData.put("email", mAuth.getCurrentUser().getEmail());
        userData.put("role", role);

        userRef.setValue(userData)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(RegisterActivity.this, "Usuario registrado exitosamente",
                            Toast.LENGTH_SHORT).show();
                    startActivity(new Intent(RegisterActivity.this, UserActivity.class));
                    finish();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(RegisterActivity.this, "Error al guardar datos",
                            Toast.LENGTH_SHORT).show();
                });
    }
    private void updateFCMToken() {
        FirebaseMessaging.getInstance().getToken()
                .addOnCompleteListener(task -> {
                    if (!task.isSuccessful()) {
                        Log.w("RegisterActivity", "Error al obtener token FCM", task.getException());
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