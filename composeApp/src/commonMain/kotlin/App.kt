import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.Button
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import io.ktor.network.selector.SelectorManager
import io.ktor.network.sockets.aSocket
import io.ktor.network.sockets.openReadChannel
import io.ktor.network.sockets.openWriteChannel
import io.ktor.utils.io.readUTF8Line
import io.ktor.utils.io.writeStringUtf8
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


@Composable
fun App() {
    MaterialTheme {
        val ipAddress = remember { mutableStateOf(TextFieldValue()) }
        val textState = remember { mutableStateOf(TextFieldValue()) }
        val textReceive: MutableState<String?>  = remember { mutableStateOf("") }
        val scope = rememberCoroutineScope()
        Column(
            Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {

            TextField(
                value = ipAddress.value,
                onValueChange = { ipAddress.value = it },
                label = { Text("Enter IP Address") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
            )
            Spacer(Modifier.padding(20.dp))
            TextField(
                value = textState.value,
                onValueChange = { textState.value = it },
                label = { Text("Enter your message") }, singleLine = true
            )
            Spacer(Modifier.padding(20.dp))
            Text("Status : ")
            Spacer(Modifier.padding(20.dp))
            Row(
                Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                Button(onClick = {
                    scope.launch {
                        withContext(Dispatchers.IO) {
                            sendMessage(textState.value.text, ipAddress.value.text)
                        }
                    }
                }) {
                    Text("Send Message")
                }
                Button(onClick = {
                    scope.launch {
                        withContext(Dispatchers.IO) {
                            getMessage(ipAddress.value.text, textReceive)
                        }
                    }
                }) {
                    Text("Get Response")
                }
            }


        }
    }
}


suspend fun sendMessage(message: String, ipAddress: String) {
    try {
        val selectorManager = SelectorManager(Dispatchers.IO)

        val socket = aSocket(selectorManager).tcp().connect(ipAddress, 8080)
        println("Now Connecting to ${socket.remoteAddress}")
        val sendChannel = socket.openWriteChannel(autoFlush = true)
        sendChannel.writeStringUtf8("$message\n")

    } catch (e: Exception) {
        println("Failed to send message: ${e.message}")
    }
}

suspend fun getMessage(ipAddress: String, textReceive: MutableState<String?>) {
    try {
        val selectorManager = SelectorManager(Dispatchers.IO)

        val socket = aSocket(selectorManager).tcp().connect(ipAddress, 8080)
        println("Now Connecting to ${socket.remoteAddress}")
        val receiveChannel = socket.openReadChannel()
        val message: String? = receiveChannel.readUTF8Line()
        textReceive.value = message

        println(textReceive.value)
    } catch (e: Exception) {
        println("Failed to get message: ${e.message}")
    }

}