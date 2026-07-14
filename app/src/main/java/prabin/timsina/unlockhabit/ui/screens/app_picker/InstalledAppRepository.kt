package prabin.timsina.unlockhabit.ui.screens.app_picker

import android.content.Context

interface InstalledAppRepository {
    suspend fun getInstalledLauncherApps(context: Context): List<AppInfo>
    fun getAppInfo(context: Context, packageName: String): AppInfo?
}
