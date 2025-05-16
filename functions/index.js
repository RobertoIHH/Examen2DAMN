const functions = require('firebase-functions');
const admin = require('firebase-admin');
admin.initializeApp();

exports.sendNotification = functions.https.onCall(async (data, context) => {
  // Verificar si el usuario está autenticado
  if (!context.auth) {
    throw new functions.https.HttpsError('unauthenticated', 'El usuario debe estar autenticado');
  }

  // Verificar si el usuario es administrador
  const userSnapshot = await admin.database().ref(`users/${context.auth.uid}`).once('value');
  const userData = userSnapshot.val();
  
  if (!userData || userData.role !== 'admin') {
    throw new functions.https.HttpsError('permission-denied', 'Solo administradores pueden enviar notificaciones');
  }

  const { recipientIds, title, body } = data;
  
  // Para cada destinatario, obtener token y enviar notificación
  const promises = recipientIds.map(async (userId) => {
    const tokenSnapshot = await admin.database().ref(`tokens/${userId}`).once('value');
    const token = tokenSnapshot.val();
    
    if (!token) return null;
    
    const message = {
      token: token,
      data: {
        title: title,
        body: body,
        senderId: context.auth.uid
      }
    };
    
    return admin.messaging().send(message);
  });
  
  await Promise.all(promises);
  return { success: true };
});