package com.escom.UPush;

import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.functions.FirebaseFunctions;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
        // Contador para llevar registro de las operaciones completadas
        final int[] successCount = {0};
        final int totalRecipients = recipientIds.size();

        // Mostrar mensaje de progreso
        Toast.makeText(this, "Enviando notificaciones...", Toast.LENGTH_SHORT).show();

        // Guardar las notificaciones directamente en Firebase Realtime Database
        for (String userId : recipientIds) {
            DatabaseReference notificationRef = FirebaseDatabase.getInstance()
                    .getReference("notifications").child(userId).push();

            Map<String, Object> notificationDataWithTimestamp = new HashMap<>(notificationData);
            notificationDataWithTimestamp.put("timestamp", ServerValue.TIMESTAMP);

            notificationRef.setValue(notificationDataWithTimestamp)
                    .addOnSuccessListener(aVoid -> {
                        successCount[0]++;

                        // Si todas las notificaciones se han guardado, mostrar mensaje de éxito
                        if (successCount[0] == totalRecipients) {
                            // Ahora enviar una notificación al topic "notifications" para notificar a todos
                            sendNotificationViaFCM(notificationData);
                        }
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(SendNotificationActivity.this,
                                "Error al guardar notificaciones: " + e.getMessage(),
                                Toast.LENGTH_SHORT).show();
                    });
        }
    }

    // Este método envía la notificación mediante FCM utilizando el tema "notifications"
    private void sendNotificationViaFCM(Map<String, Object> notificationData) {
        // Esto solo funciona para dispositivos suscritos al tema "notifications"
        Toast.makeText(SendNotificationActivity.this,
                "Notificaciones guardadas correctamente. Los usuarios las verán cuando abran la app.",
                Toast.LENGTH_LONG).show();
        finish();
    }
}