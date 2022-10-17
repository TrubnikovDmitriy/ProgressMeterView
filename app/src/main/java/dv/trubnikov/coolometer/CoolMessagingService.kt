package dv.trubnikov.coolometer

import android.util.Log
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

class CoolMessagingService : FirebaseMessagingService() {

    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)
        Log.i("KekPek", "Receive message ${message.data}")
    }

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        Log.w("KekPek", "onNewToken($token)")
    }
}
