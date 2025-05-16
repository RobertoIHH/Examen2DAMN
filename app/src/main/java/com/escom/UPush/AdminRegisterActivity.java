package com.escom.UPush;

import android.content.Intent;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import java.util.HashMap;
import java.util.Map;
public class AdminRegisterActivity extends AppCompatActivity {
    private FirebaseAuth mAuth;
    private EditText emailEditText, passwordEditText, nameEditText, masterPasswordEditText;
    private static final String MASTER_PASSWORD = "admin123"; // Define tu contraseña maestra aquí

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_register);

        mAuth = FirebaseAuth.getInstance();
        emailEditText = findViewById(R.id.email_edit_text);
        passwordEditText = findViewById(R.id.password_edit_text);
        nameEditText = findViewById(R.id.name_edit_text);
        masterPasswordEditText = findViewById(R.id.master_password_edit_text);

        findViewById(R.id.register_admin_button).setOnClickListener(v -> registerAdmin());
    }

    private void registerAdmin() {
        String masterPassword = masterPasswordEditText.getText().toString();

        if (!MASTER_PASSWORD.equals(masterPassword)) {
            Toast.makeText(this, "Contraseña maestra incorrecta",
                    Toast.LENGTH_SHORT).show();
            return;
        }

        String email = emailEditText.getText().toString();
        String password = passwordEditText.getText().toString();
        String name = nameEditText.getText().toString();

        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        saveUserData(name, "admin");
                    } else {
                        Toast.makeText(AdminRegisterActivity.this, "Registro fallido",
                                Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void saveUserData(String name, String role) {
        // Mismo código que en RegisterActivity pero guardando role="admin"
        String userId = mAuth.getCurrentUser().getUid();
        DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("users").child(userId);

        Map<String, Object> userData = new HashMap<>();
        userData.put("name", name);
        userData.put("email", mAuth.getCurrentUser().getEmail());
        userData.put("role", role);

        userRef.setValue(userData)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(AdminRegisterActivity.this, "Administrador registrado exitosamente",
                            Toast.LENGTH_SHORT).show();
                    startActivity(new Intent(AdminRegisterActivity.this, AdminActivity.class));
                    finish();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(AdminRegisterActivity.this, "Error al guardar datos",
                            Toast.LENGTH_SHORT).show();
                });
    }
}