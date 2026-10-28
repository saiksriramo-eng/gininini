package app.ginininini.ui

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp

@Composable
fun PermissionsScreen(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val scrollState = rememberScrollState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(scrollState),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "Permissions & Setup",
            style = MaterialTheme.typography.headlineMedium
        )
        Text(
            text = "Android 16 requires specific permissions for the overlay bubble and capture features. Tap each to configure.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        PermissionRow(
            title = "1. Display over other apps",
            desc = "Required to show the floating bubble and arc menu.",
            buttonText = "Open Overlay Settings",
            onClick = {
                val intent = Intent(
                    Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                    Uri.parse("package:${context.packageName}")
                )
                context.startActivity(intent)
            }
        )

        PermissionRow(
            title = "2. Sideloaded App Restricted Settings",
            desc = "On Android 13-16, accessibility is locked until you tap 'Allow restricted settings' in App Info (3 dots at top-right).",
            buttonText = "Open App Info",
            onClick = {
                val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                    data = Uri.fromParts("package", context.packageName, null)
                }
                context.startActivity(intent)
            }
        )

        PermissionRow(
            title = "3. Accessibility Service",
            desc = "Enables one-gesture capture of highlighted song/artist text across apps.",
            buttonText = "Open Accessibility Settings",
            onClick = {
                val intent = Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)
                context.startActivity(intent)
            }
        )

        PermissionRow(
            title = "4. Notification Listener (Phase 3)",
            desc = "Allows reading the active YouTube Music track for Now-Playing controls.",
            buttonText = "Open Notification Access",
            onClick = {
                val intent = Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS)
                context.startActivity(intent)
            }
        )

        PermissionRow(
            title = "5. Battery Optimization",
            desc = "Set battery usage to Unrestricted so the bubble service is never stopped by Moto battery saver.",
            buttonText = "Open Battery Settings",
            onClick = {
                val intent = Intent(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS)
                context.startActivity(intent)
            }
        )
    }
}

@Composable
fun PermissionRow(
    title: String,
    desc: String,
    buttonText: String,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = title, style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = desc, style = MaterialTheme.typography.bodySmall)
            Spacer(modifier = Modifier.height(12.dp))
            Button(onClick = onClick) {
                Text(buttonText)
            }
        }
    }
}
