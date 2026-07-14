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
import prabin.timsina.unlockhabit.utils.ApplicationScope
import javax.inject.Inject

@AndroidEntryPoint
class MainService : Service() {

    @Inject
    lateinit var serviceTracker: ServiceTracker

    @Inject
    lateinit var appNotificationManager: AppNotificationManager

    @Inject
    @ApplicationScope
    lateinit var scope: CoroutineScope

    @Inject
    lateinit var screenReceiver: ScreenUnlockReceiver

    override fun onCreate() {
        super.onCreate()
        serviceTracker.setServiceRunning(true)
        startAsForeground()

        val filter = IntentFilter(Intent.ACTION_USER_PRESENT)
        registerReceiver(screenReceiver, filter)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            AppNotificationManager.ACTION_STOP -> {
                stopSelf()
            }

            AppNotificationManager.ACTION_PAUSE -> {
                scope.launch {
                    val newPausedState = !serviceTracker.isPaused.value
                    serviceTracker.setPaused(newPausedState)
                    appNotificationManager.notifyMainServiceFGSNotification(newPausedState)
                }
            }
        }

        return START_STICKY
    }

    private fun startAsForeground() {
        val notification = appNotificationManager.createMainServiceFGSNotification(
            isPaused = serviceTracker.isPaused.value
        )
        startForeground(FGS_NOTIFICATION_ID, notification)
    }

    override fun onBind(p0: Intent?): IBinder? = null

    override fun onDestroy() {
        super.onDestroy()
        serviceTracker.setServiceRunning(false)
        serviceTracker.setPaused(false)
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
