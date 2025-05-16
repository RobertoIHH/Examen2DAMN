public class SendNotificationActivity extends AppCompatActivity {
    private Spinner userSpinner;
    private EditText titleEditText, messageEditText;
    private CheckBox selectAllCheckBox;
    private List<User> userList = new ArrayList<>();
    private List<String> userIds = new ArrayList<>();
    private ArrayAdapter<String> adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_send_notification);

        userSpinner = findViewById(R.id.user_spinner);
        titleEditText = findViewById(R.id.title_edit_text);
        messageEditText = findViewById(R.id.message_edit_text);
        selectAllCheckBox = findViewById(R.id.select_all_checkbox);

        loadUsers();

        findViewById(R.id.send_button).setOnClickListener(v -> sendNotification());
        selectAllCheckBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
            userSpinner.setEnabled(!isChecked);
        });
    }

    private void loadUsers() {
        DatabaseReference usersRef = FirebaseDatabase.getInstance().getReference("users");
        usersRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                userList.clear();
                userIds.clear();
                List<String> userNames = new ArrayList<>();

                for (DataSnapshot userSnapshot : snapshot.getChildren()) {
                    User user = userSnapshot.getValue(User.class);
                    user.setId(userSnapshot.getKey());
                    userList.add(user);
                    userIds.add(user.getId());
                    userNames.add(user.getName());
                }

                adapter = new ArrayAdapter<>(SendNotificationActivity.this,
                        android.R.layout.simple_spinner_item, userNames);
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                userSpinner.setAdapter(adapter);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(SendNotificationActivity.this, "Error al cargar usuarios",
                        Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void sendNotification() {
        String title = titleEditText.getText().toString();
        String message = messageEditText.getText().toString();

        if (title.isEmpty() || message.isEmpty()) {
            Toast.makeText(this, "Título y mensaje son requeridos", Toast.LENGTH_SHORT).show();
            return;
        }

        // Crear los datos de la notificación
        Map<String, Object> notificationData = new HashMap<>();
        notificationData.put("title", title);
        notificationData.put("body", message);
        notificationData.put("senderId", FirebaseAuth.getInstance().getCurrentUser().getUid());

        // Determinar destinatarios
        List<String> recipientIds = new ArrayList<>();

        if (selectAllCheckBox.isChecked()) {
            // Enviar a todos los usuarios
            recipientIds.addAll(userIds);
        } else {
            // Enviar solo al usuario seleccionado
            int selectedPosition = userSpinner.getSelectedItemPosition();
            if (selectedPosition >= 0 && selectedPosition < userIds.size()) {
                recipientIds.add(userIds.get(selectedPosition));
            }
        }

        // Usar Cloud Functions (necesitarás implementarlo) o HTTP Function
        sendNotificationToRecipients(recipientIds, notificationData);
    }

    private void sendNotificationToRecipients(List<String> recipientIds, Map<String, Object> notificationData) {
        FirebaseFunctions functions = FirebaseFunctions.getInstance();

        Map<String, Object> data = new HashMap<>();
        data.put("recipientIds", recipientIds);
        data.put("title", notificationData.get("title"));
        data.put("body", notificationData.get("body"));

        functions.getHttpsCallable("sendNotification")
                .call(data)
                .addOnSuccessListener(result -> {
                    Toast.makeText(SendNotificationActivity.this,
                            "Notificaciones enviadas correctamente",
                            Toast.LENGTH_SHORT).show();
                    finish();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(SendNotificationActivity.this,
                            "Error al enviar notificaciones: " + e.getMessage(),
                            Toast.LENGTH_SHORT).show();
                });
    }
}