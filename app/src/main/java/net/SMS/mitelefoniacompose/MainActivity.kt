@file:Suppress("DEPRECATION")

package net.ivanvega.mitelefoniacompose

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.provider.Telephony
import android.telephony.SmsManager
import android.telephony.SmsMessage
import android.telephony.TelephonyManager
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.viewmodel.compose.viewModel
import net.ivanvega.mitelefoniacompose.ui.theme.MiTelefoniaComposeTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MiTelefoniaComposeTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    HomeScreen()
                }
            }
        }
    }
}
@Composable
fun SystemBroadcastReceiver(systemAction: String,
    onSystemEvent: (intent: Intent?) -> Unit
) {
    // Grab the current context in this part of the UI tree
    val context = LocalContext.current
    // Safely use the latest onSystemEvent lambda passed to the function
    val currentOnSystemEvent by rememberUpdatedState(onSystemEvent)
    // If either context or systemAction changes, unregister and register again
    DisposableEffect(context, systemAction) {
        val intentFilter = IntentFilter(systemAction)
        val broadcast = object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                currentOnSystemEvent(intent) } }
        context.registerReceiver(broadcast, intentFilter)
        // When the effect leaves the Composition, remove the callback
        onDispose {
            context.unregisterReceiver(broadcast)
        }
    }
}

@Composable
fun HomeScreen() {
    val context = LocalContext.current
    val phoneNumberState = remember { mutableStateOf(TextFieldValue()) }
    val messageState = remember { mutableStateOf(TextFieldValue()) }
    Column {
        TextField(
            value = phoneNumberState.value,
            onValueChange = { phoneNumberState.value = it },
            label = { Text("Phone Number") }
        )
        TextField(
            value = messageState.value,
            onValueChange = { messageState.value = it },
            label = { Text("Message") }
        )

        SystemBroadcastReceiver(TelephonyManager.ACTION_PHONE_STATE_CHANGED) { intent ->
            val phoneNumber = intent?.getStringExtra(TelephonyManager.EXTRA_INCOMING_NUMBER)
            if (phoneNumber == phoneNumberState.value.text) {
                sendSMS(context, phoneNumberState.value.text, messageState.value.text)
            }
        }
    }
}

private fun sendSMS(context: Context, phoneNumber: String, message: String) {
    SmsManager.getDefault().sendTextMessage(phoneNumber, null, message, null, null)
}


