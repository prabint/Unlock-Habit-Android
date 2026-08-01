package prabin.timsina.unlockhabit.broadcasts

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import prabin.timsina.unlockhabit.repository.UserPreferencesRepository
import prabin.timsina.unlockhabit.services.MainService
import prabin.timsina.unlockhabit.services.PausedTracker
import prabin.timsina.unlockhabit.ui.screens.app_picker.InstalledAppRepository
import prabin.timsina.unlockhabit.ui.screens.overlay.OverlayController
import prabin.timsina.unlockhabit.utils.ApplicationScope
import prabin.timsina.unlockhabit.utils.MainDispatcher
import timber.log.Timber
import javax.inject.Inject

class ScreenUnlockReceiver @Inject constructor(
    private val repository: UserPreferencesRepository,
    private val pausedTracker: PausedTracker,
    @param:ApplicationScope private val scope: CoroutineScope,
    private val installedAppRepository: InstalledAppRepository,
    private val overlayController: OverlayController,
    @param:MainDispatcher private val main: CoroutineDispatcher,
) : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_USER_PRESENT) {
            if (pausedTracker.isPaused.value) return
            val pendingResult = goAsync()
            scope.launch {
                try {
                    val pkg = repository.autoLaunchPackage.firstOrNull() ?: return@launch
                    val shouldLaunchDirectly = repository.shouldLaunchDirectly.firstOrNull() ?: true
                    val appInfo = installedAppRepository.getAppInfo(context, pkg) ?: return@launch
                    if (shouldLaunchDirectly) {
                        launchApp(context, pkg)
                    } else {
                        withContext(main) {
                            overlayController.showView(appInfo)
                        }
                    }
                } catch (e: Exception) {
                    Timber.e(e, "Failed to handle screen unlock")
                } finally {
                    pendingResult.finish()
                }
            }
        }
    }
}

fun launchApp(context: Context, packageName: String) {
    val launchIntent = context.packageManager.getLaunchIntentForPackage(packageName)
    if (launchIntent != null) {
        launchIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(launchIntent)
    } else {
        Timber.w("App not found.")
    }
}
