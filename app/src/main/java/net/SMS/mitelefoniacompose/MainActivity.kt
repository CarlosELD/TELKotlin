@file:Suppress("DEPRECATION")

package net.ivanvega.mitelefoniacompose

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.telephony.TelephonyManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
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
fun HomeScreen(screenViewModel: ScreenViewModel = viewModel()) {
    val context = LocalContext.current
    Surface (modifier = Modifier.fillMaxSize(), color= Color.Cyan){
        Column(modifier = Modifier,
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally) {
            val phoneNumberState = screenViewModel.phoneNumber.value
            val messageState = screenViewModel.message.value
            TextField(
                value = phoneNumberState,
                onValueChange = { screenViewModel.setPhoneNumber(it.text) },
                label = { Text("Phone Number") },
                enabled = true, // Habilitar la edición del número de teléfono
                visualTransformation = VisualTransformation.None // Para evitar la inversión del texto
            )
            Spacer(modifier = Modifier.padding(10.dp))
            TextField(
                value = messageState,
                onValueChange = { screenViewModel.setMessage(it.text) },
                label = { Text("Message") },
                enabled = true, // Habilitar la edición del mensaje
                visualTransformation = VisualTransformation.None // Para evitar la inversión del texto
            )
            Button(
                onClick = {
                    screenViewModel.sendSMS(
                        context,
                        phoneNumberState.text,
                        messageState.text
                    )
                },
                modifier = Modifier.padding(top = 16.dp)
            ) {
                Text("Send SMS")
            }
            SystemBroadcastReceiver(TelephonyManager.ACTION_PHONE_STATE_CHANGED) { intent ->
                val phoneNumber = intent?.getStringExtra(TelephonyManager.EXTRA_INCOMING_NUMBER)
                if (phoneNumber == phoneNumberState.text) {
                    screenViewModel.sendSMS(context, phoneNumberState.text, messageState.text)
                }
            }
        }
    }
}

