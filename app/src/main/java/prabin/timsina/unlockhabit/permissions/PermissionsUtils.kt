package prabin.timsina.unlockhabit.permissions

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.core.content.ContextCompat
import androidx.core.content.PermissionChecker.PERMISSION_GRANTED
import androidx.core.net.toUri

fun isPermissionGranted(context: Context, permission: String): Boolean {
    return ContextCompat.checkSelfPermission(context, permission) == PERMISSION_GRANTED
}

fun isDrawOverPermissionGranted(context: Context): Boolean {
    return Settings.canDrawOverlays(context)
}

fun Activity?.launchAppSettingsIntent() = this?.startActivity(
    Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
        .apply {
            data = Uri.fromParts(
                "package",
                this@launchAppSettingsIntent.packageName,
                null
            )
        }
)

fun Activity.tryRequestOverlayPermission() {
    if (!Settings.canDrawOverlays(this)) {
        val intent = Intent(
            Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
            "package:$packageName".toUri()
        )
        startActivity(intent)
    }
}
