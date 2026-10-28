package app.ginininini.ui

import android.content.Intent
import android.provider.Settings
import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import app.ginininini.data.SettingsStore
import app.ginininini.overlay.BubbleService
import kotlinx.coroutines.launch

@Composable
fun SettingsScreen(
    settingsStore: SettingsStore,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val scrollState = rememberScrollState()

    val backendUrl by settingsStore.backendUrl.collectAsState(initial = "http://192.168.1.100:8000")
    val backendToken by settingsStore.backendToken.collectAsState(initial = "dev-token")
    val expirySeconds by settingsStore.expirySeconds.collectAsState(initial = 60)
    val inboxName by settingsStore.inboxPlaylist.collectAsState(initial = "Discovery Inbox")
    val artistsName by settingsStore.artistsPlaylist.collectAsState(initial = "Artists to Explore")

    var urlInput by remember(backendUrl) { mutableStateOf(backendUrl) }
    var tokenInput by remember(backendToken) { mutableStateOf(backendToken) }
    var expiryInput by remember(expirySeconds) { mutableStateOf(expirySeconds.toString()) }
    var inboxInput by remember(inboxName) { mutableStateOf(inboxName) }
    var artistsInput by remember(artistsName) { mutableStateOf(artistsName) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(scrollState),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Text("Backend & Preferences", style = MaterialTheme.typography.headlineMedium)

        OutlinedTextField(
            value = urlInput,
            onValueChange = { urlInput = it },
            label = { Text("Backend URL (e.g. http://192.168.x.x:8000)") },
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = tokenInput,
            onValueChange = { tokenInput = it },
            label = { Text("Shared Secret Token") },
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = expiryInput,
            onValueChange = { expiryInput = it },
            label = { Text("Selection Expiry (seconds)") },
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = inboxInput,
            onValueChange = { inboxInput = it },
            label = { Text("Song Playlist Name") },
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = artistsInput,
            onValueChange = { artistsInput = it },
            label = { Text("Artist Playlist Name") },
            modifier = Modifier.fillMaxWidth()
        )

        Button(
            onClick = {
                scope.launch {
                    settingsStore.setBackendUrl(urlInput.trim())
                    settingsStore.setBackendToken(tokenInput.trim())
                    expiryInput.toIntOrNull()?.let { settingsStore.setExpirySeconds(it) }
                    settingsStore.setInboxPlaylist(inboxInput.trim())
                    settingsStore.setArtistsPlaylist(artistsInput.trim())
                    Toast.makeText(context, "Settings saved!", Toast.LENGTH_SHORT).show()
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Save Settings")
        }

        Divider(modifier = Modifier.padding(vertical = 8.dp))

        Text("Overlay Controls", style = MaterialTheme.typography.titleMedium)

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Button(
                onClick = {
                    if (!Settings.canDrawOverlays(context)) {
                        Toast.makeText(context, "Please allow Overlay permission first", Toast.LENGTH_LONG).show()
                    } else {
                        val startIntent = Intent(context, BubbleService::class.java).apply {
                            action = BubbleService.ACTION_START
                        }
                        context.startForegroundService(startIntent)
                    }
                },
                modifier = Modifier.weight(1f)
            ) {
                Text("Start Bubble")
            }

            OutlinedButton(
                onClick = {
                    val stopIntent = Intent(context, BubbleService::class.java).apply {
                        action = BubbleService.ACTION_STOP
                    }
                    context.startService(stopIntent)
                },
                modifier = Modifier.weight(1f)
            ) {
                Text("Stop Bubble")
            }
        }
    }
}
