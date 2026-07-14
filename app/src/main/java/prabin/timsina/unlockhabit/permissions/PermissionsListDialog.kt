package prabin.timsina.unlockhabit.permissions

import android.Manifest
import android.os.Build
import android.provider.Settings
import androidx.activity.compose.LocalActivity
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import com.google.accompanist.permissions.rememberPermissionState
import com.google.accompanist.permissions.shouldShowRationale
import prabin.timsina.unlockhabit.R
import prabin.timsina.unlockhabit.ui.theme.Green
import prabin.timsina.unlockhabit.ui.theme.PreviewWrapper

@Composable
fun PermissionsListDialog(onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = { onDismiss() },
        confirmButton = {
            TextButton(onClick = { onDismiss() }) {
                Text(stringResource(R.string.dismiss))
            }
        },
        title = { Text(stringResource(R.string.permissions_required)) },
        text = {
            PermissionsList()
        }
    )
}

@Composable
private fun PermissionsList() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        NotificationPermissionRow()

        HorizontalDivider()

        OverlayPermissionRow()
    }
}

@OptIn(ExperimentalPermissionsApi::class)
@Composable
private fun NotificationPermissionRow() {
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) return

    val activity = LocalActivity.current
    val notificationPermission = rememberPermissionState(Manifest.permission.POST_NOTIFICATIONS)
    val isNotificationPermissionGranted = notificationPermission.status.isGranted
    val shouldShowRationale = notificationPermission.status.shouldShowRationale

    ListItem(
        modifier = Modifier.clickable(
            enabled = !isNotificationPermissionGranted,
            onClick = {
                if (shouldShowRationale) {
                    notificationPermission.launchPermissionRequest()
                } else {
                    activity?.launchAppSettingsIntent()
                }
            }
        ),
        colors = ListItemDefaults.colors(containerColor = Color.Transparent),
        headlineContent = { Text(text = stringResource(R.string.post_notification_title)) },
        overlineContent = {
            Text(
                text = if (isNotificationPermissionGranted) stringResource(R.string.granted) else stringResource(
                    R.string.not_granted
                )
            )
        },
        supportingContent = {
            Text(
                text = if (isNotificationPermissionGranted) "" else stringResource(R.string.notification_permission_rationale)
            )
        },
        leadingContent = {
            if (isNotificationPermissionGranted) {
                Icon(
                    imageVector = Icons.Default.Check,
                    tint = Green,
                    contentDescription = null
                )
            } else {
                Icon(
                    imageVector = Icons.Default.Close,
                    tint = Color.Red,
                    contentDescription = null,
                )
            }
        },
        trailingContent = {
            if (!isNotificationPermissionGranted) {
                Icon(Icons.Default.ChevronRight, null)
            }
        }
    )
}

@Composable
private fun OverlayPermissionRow() {
    val context = LocalContext.current
    val activity = LocalActivity.current
    val lifecycleOwner = LocalLifecycleOwner.current

    var isOverlayPermissionGranted by remember { mutableStateOf(Settings.canDrawOverlays(context)) }

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                isOverlayPermissionGranted = Settings.canDrawOverlays(context)
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    ListItem(
        modifier = Modifier.clickable(
            enabled = !isOverlayPermissionGranted,
            onClick = {
                activity?.tryRequestOverlayPermission()
            }
        ),
        colors = ListItemDefaults.colors(containerColor = Color.Transparent),
        headlineContent = { Text(text = stringResource(R.string.display_over_other_apps_title)) },
        overlineContent = {
            Text(
                text = if (isOverlayPermissionGranted) stringResource(R.string.granted)
                else stringResource(R.string.not_granted)
            )
        },
        supportingContent = {
            Text(
                text = if (isOverlayPermissionGranted) ""
                else stringResource(R.string.display_over_other_apps_rationale)
            )
        },
        leadingContent = {
            if (isOverlayPermissionGranted) {
                Icon(
                    imageVector = Icons.Default.Check,
                    tint = Green,
                    contentDescription = null,
                )
            } else {
                Icon(
                    imageVector = Icons.Default.Close,
                    tint = Color.Red,
                    contentDescription = null,
                )
            }
        },
        trailingContent = {
            if (!isOverlayPermissionGranted) {
                Icon(Icons.Default.ChevronRight, null)
            }
        }
    )
}

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun RequestAllPermissions() {
    val permissions = buildList {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            add(Manifest.permission.POST_NOTIFICATIONS)
        }
    }
    val allPermissions = rememberMultiplePermissionsState(permissions)
    LaunchedEffect(Unit) {
        allPermissions.launchMultiplePermissionRequest()
    }
}

@Preview(showBackground = true)
@Composable
private fun PermissionsListPreview() {
    PreviewWrapper {
        PermissionsList()
    }
}
