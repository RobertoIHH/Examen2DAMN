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
}