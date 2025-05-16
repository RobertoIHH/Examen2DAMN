package com.escom.UPush;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.auth.FirebaseAuth;

public class UserActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user);

        Button viewProfileButton = findViewById(R.id.view_profile_button);
        Button viewNotificationsButton = findViewById(R.id.view_notifications_button);
        Button logoutButton = findViewById(R.id.logout_button);

        viewProfileButton.setOnClickListener(v -> {
            // Implementar visualización de perfil
        });

        viewNotificationsButton.setOnClickListener(v -> {
            startActivity(new Intent(UserActivity.this, NotificationsActivity.class));
        });

        logoutButton.setOnClickListener(v -> {
            FirebaseAuth.getInstance().signOut();
            startActivity(new Intent(UserActivity.this, LoginActivity.class));
            finish();
        });
    }
}