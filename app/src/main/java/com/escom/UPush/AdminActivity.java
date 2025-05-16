package com.escom.UPush;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.auth.FirebaseAuth;

public class AdminActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin);

        Button viewUsersButton = findViewById(R.id.view_users_button);
        Button sendNotificationButton = findViewById(R.id.send_notification_button);
        Button logoutButton = findViewById(R.id.logout_button);

        viewUsersButton.setOnClickListener(v -> {
            // Implementar visualización de usuarios
        });

        sendNotificationButton.setOnClickListener(v -> {
            startActivity(new Intent(AdminActivity.this, SendNotificationActivity.class));
        });

        logoutButton.setOnClickListener(v -> {
            FirebaseAuth.getInstance().signOut();
            startActivity(new Intent(AdminActivity.this, LoginActivity.class));
            finish();
        });
    }
}