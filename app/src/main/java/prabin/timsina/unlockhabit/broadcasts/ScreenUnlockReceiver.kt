package prabin.timsina.unlockhabit.broadcasts

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import prabin.timsina.unlockhabit.repository.DefaultUserPreferencesRepository.Companion.MAX_HEARTS
import prabin.timsina.unlockhabit.repository.UserPreferencesRepository
import prabin.timsina.unlockhabit.services.PausedTracker
import prabin.timsina.unlockhabit.ui.screens.app_picker.InstalledAppRepository
import prabin.timsina.unlockhabit.ui.screens.overlay.slider.SliderOverlayController
import prabin.timsina.unlockhabit.utils.ApplicationScope
import prabin.timsina.unlockhabit.utils.MainDispatcher
import timber.log.Timber
import java.time.LocalDate
import java.time.ZoneId
import javax.inject.Inject

class ScreenUnlockReceiver @Inject constructor(
    private val repository: UserPreferencesRepository,
    private val pausedTracker: PausedTracker,
    @param:ApplicationScope private val scope: CoroutineScope,
    private val installedAppRepository: InstalledAppRepository,
    private val sliderOverlayController: SliderOverlayController,
    @param:MainDispatcher private val main: CoroutineDispatcher,
) : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_USER_PRESENT) {
            if (pausedTracker.isPaused.value) return
            val pendingResult = goAsync()
            scope.launch {
                try {
                    val preferences = repository.preferences.first()
                    val pkg = preferences.autoLaunchPackage ?: return@launch
                    val shouldLaunchDirectly = preferences.shouldLaunchDirectly
                    val resetAt = preferences.heartsResetAt
                    if (resetAt <= System.currentTimeMillis()) {
                        repository.setHeartsLeft(MAX_HEARTS)
                        repository.setHeartsResetAt(nextMidnightMillis())
                    }
                    val compareCountWithYesterday = repository.getTodayAndYesterdayUnlockCount()
                    if (shouldLaunchDirectly) {
                        launchApp(context, pkg)
                    } else {
                        val appInfo = installedAppRepository.getAppInfo(context, pkg) ?: return@launch
                        val heartsLeft = preferences.heartsLeft
                        withContext(main) {
                            sliderOverlayController.show(
                                appInfo = appInfo,
                                heartsLeft = heartsLeft,
                                todayYesterdayCounts = compareCountWithYesterday,
                            )
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

fun nextMidnightMillis(): Long =
    LocalDate.now()
        .plusDays(1)
        .atStartOfDay(ZoneId.systemDefault())
        .toInstant()
        .toEpochMilli()
