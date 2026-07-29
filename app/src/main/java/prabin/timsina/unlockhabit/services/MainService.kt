package prabin.timsina.unlockhabit.services

import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.IBinder
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import prabin.timsina.unlockhabit.broadcasts.ScreenUnlockReceiver
import prabin.timsina.unlockhabit.notifications.AppNotificationManager
import prabin.timsina.unlockhabit.notifications.AppNotificationManager.Companion.FGS_NOTIFICATION_ID
import prabin.timsina.unlockhabit.repository.UserPreferencesRepository
import prabin.timsina.unlockhabit.utils.ApplicationScope
import javax.inject.Inject

@AndroidEntryPoint
class MainService : Service() {

    @Inject
    lateinit var pausedTracker: PausedTracker

    @Inject
    lateinit var appNotificationManager: AppNotificationManager

    @Inject
    @ApplicationScope
    lateinit var scope: CoroutineScope

    @Inject
    lateinit var screenReceiver: ScreenUnlockReceiver

    @Inject
    lateinit var userPreferencesRepository: UserPreferencesRepository

    override fun onCreate() {
        super.onCreate()
        startAsForeground()

        val filter = IntentFilter(Intent.ACTION_USER_PRESENT)
        registerReceiver(screenReceiver, filter)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            AppNotificationManager.ACTION_STOP -> {
                scope.launch { userPreferencesRepository.setUserEnabledService(enabled = false) }
                stopSelf()
            }

            AppNotificationManager.ACTION_PAUSE -> {
                scope.launch {
                    val newPausedState = !pausedTracker.isPaused.value
                    pausedTracker.setFunctionalityPaused(newPausedState)
                    appNotificationManager.notifyMainServiceFGSNotification(newPausedState)
                }
            }
        }

        return START_STICKY
    }

    private fun startAsForeground() {
        val notification = appNotificationManager.createMainServiceFGSNotification(isPaused = false)
        startForeground(FGS_NOTIFICATION_ID, notification)
    }

    override fun onBind(p0: Intent?): IBinder? = null

    override fun onDestroy() {
        super.onDestroy()
        pausedTracker.setFunctionalityPaused(false)
        unregisterReceiver(screenReceiver)
    }

    companion object {
        fun startService(context: Context) {
            val intent = Intent(context, MainService::class.java)
            context.startForegroundService(intent)
        }

        fun stopService(context: Context) {
            val intent = Intent(context, MainService::class.java)
            context.stopService(intent)
        }
    }
}
