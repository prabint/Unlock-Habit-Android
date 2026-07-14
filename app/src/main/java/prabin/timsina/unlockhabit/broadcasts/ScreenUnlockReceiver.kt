package prabin.timsina.unlockhabit.broadcasts

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import prabin.timsina.unlockhabit.repository.UserPreferencesRepository
import prabin.timsina.unlockhabit.services.ServiceTracker
import prabin.timsina.unlockhabit.utils.ApplicationScope
import timber.log.Timber
import javax.inject.Inject

class ScreenUnlockReceiver @Inject constructor(
    private val repository: UserPreferencesRepository,
    private val serviceTracker: ServiceTracker,
    @param:ApplicationScope private val scope: CoroutineScope,
) : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_USER_PRESENT) {
            scope.launch {
                val pkg = repository.autoLaunchPackage.firstOrNull()
                if (pkg != null && !serviceTracker.isPaused.value) {
                    launchApp(context, pkg)
                }
            }
        }
    }

    private fun launchApp(context: Context, packageName: String) {
        val launchIntent = context.packageManager.getLaunchIntentForPackage(packageName)
        if (launchIntent != null) {
            launchIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(launchIntent)
        } else {
            Timber.w("App not found.")
        }
    }
}
