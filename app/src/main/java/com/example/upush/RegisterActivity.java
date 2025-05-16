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
}