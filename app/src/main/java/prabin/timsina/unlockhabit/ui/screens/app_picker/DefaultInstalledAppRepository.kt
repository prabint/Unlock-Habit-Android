package prabin.timsina.unlockhabit.ui.screens.app_picker

import android.content.Context
import android.content.Intent
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import prabin.timsina.unlockhabit.utils.IoDispatcher
import timber.log.Timber
import javax.inject.Inject

class DefaultInstalledAppRepository @Inject constructor(
    @param:IoDispatcher private val io: CoroutineDispatcher,
) : InstalledAppRepository {
    override suspend fun getInstalledLauncherApps(context: Context): List<AppInfo> {
        return withContext(io) {
            val pm = context.packageManager
            val intent = Intent(Intent.ACTION_MAIN, null).apply {
                addCategory(Intent.CATEGORY_LAUNCHER)
            }

            pm.queryIntentActivities(intent, 0)
                .map { it.activityInfo.packageName }
                .distinct()
                .mapNotNull { getAppInfo(context, it) }
                .sortedBy { it.name }
        }
    }

    override fun getAppInfo(context: Context, packageName: String): AppInfo? {
        return try {
            val pm = context.packageManager
            val info = pm.getApplicationInfo(packageName, 0)
            AppInfo(
                name = pm.getApplicationLabel(info).toString(),
                packageName = packageName,
                icon = pm.getApplicationIcon(info)
            )
        } catch (e: Exception) {
            Timber.e(e)
            null
        }
    }
}
