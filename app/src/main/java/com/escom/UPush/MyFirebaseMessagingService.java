package com.escom.UPush;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import java.util.HashMap;
import java.util.Map;

public class MyFirebaseMessagingService extends FirebaseMessagingService {
    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);

        // Guardar la notificación en la base de datos local
        if (remoteMessage.getData().size() > 0) {
            String title = remoteMessage.getData().get("title");
            String body = remoteMessage.getData().get("body");
            String senderId = remoteMessage.getData().get("senderId");

            // Guardar en Firebase Database para historial
            saveNotificationToDatabase(title, body, senderId);

            // Mostrar notificación
            showNotification(title, body);
        }
    }

    private void saveNotificationToDatabase(String title, String body, String senderId) {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            String userId = currentUser.getUid();
            DatabaseReference notificationRef = FirebaseDatabase.getInstance().getReference("notifications")
                    .child(userId).push();

            Map<String, Object> notificationData = new HashMap<>();
            notificationData.put("title", title);
            notificationData.put("body", body);
            notificationData.put("senderId", senderId);
            notificationData.put("timestamp", ServerValue.TIMESTAMP);

            notificationRef.setValue(notificationData);
        }
    }

    private void showNotification(String title, String body) {
        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        String channelId = "fcm_default_channel";

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    channelId,
                    "Canal de notificaciones",
                    NotificationManager.IMPORTANCE_DEFAULT);
            notificationManager.createNotificationChannel(channel);
        }

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, channelId)
                .setSmallIcon(R.drawable.ic_notification)
                .setContentTitle(title)
                .setContentText(body)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setAutoCancel(true);

        Intent intent = new Intent(this, NotificationsActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_IMMUTABLE);
        builder.setContentIntent(pendingIntent);

        notificationManager.notify(0, builder.build());
    }

    @Override
    public void onNewToken(String token) {
        super.onNewToken(token);
        // Actualizar token en database
        updateTokenInDatabase(token);
    }

    private void updateTokenInDatabase(String token) {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            DatabaseReference tokenRef = FirebaseDatabase.getInstance().getReference("tokens")
                    .child(currentUser.getUid());
            tokenRef.setValue(token);
        }
    }

    @Override
    public void onCreate() {
        super.onCreate();

        // Escuchar cambios en la base de datos de notificaciones para el usuario actual
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            String userId = currentUser.getUid();
            DatabaseReference notificationsRef = FirebaseDatabase.getInstance()
                    .getReference("notifications").child(userId);

            notificationsRef.addChildEventListener(new ChildEventListener() {
                @Override
                public void onChildAdded(@NonNull DataSnapshot snapshot, String previousChildName) {
                    if (snapshot.exists()) {
                        Notification notification = snapshot.getValue(Notification.class);
                        if (notification != null) {
                            showNotification(notification.getTitle(), notification.getBody());
                        }
                    }
                }

                @Override
                public void onChildChanged(@NonNull DataSnapshot snapshot, String previousChildName) {}

                @Override
                public void onChildRemoved(@NonNull DataSnapshot snapshot) {}

                @Override
                public void onChildMoved(@NonNull DataSnapshot snapshot, String previousChildName) {}

                @Override
                public void onCancelled(@NonNull DatabaseError error) {}
            });
        }
    }
}