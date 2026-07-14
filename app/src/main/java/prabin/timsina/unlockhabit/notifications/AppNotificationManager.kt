package prabin.timsina.unlockhabit.notifications

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import dagger.hilt.android.qualifiers.ApplicationContext
import prabin.timsina.unlockhabit.MainActivity
import prabin.timsina.unlockhabit.R
import prabin.timsina.unlockhabit.services.MainService
import javax.inject.Inject

class AppNotificationManager @Inject constructor(
    @param:ApplicationContext private val context: Context,
    private val notificationManager: NotificationManager,
) {
    companion object {
        const val MAIN_CHANNEL_ID = "main_service_channel"
        const val ACTION_STOP = "STOP_SERVICE"
        const val ACTION_PAUSE = "PAUSE_SERVICE"
        const val FGS_NOTIFICATION_ID = 11
    }

    fun initialize() {
        val channel = NotificationChannel(
            MAIN_CHANNEL_ID,
            context.getString(R.string.notification_channel_name),
            NotificationManager.IMPORTANCE_LOW
        ).apply {
            description = context.getString(R.string.notification_channel_description)
        }

        notificationManager.createNotificationChannel(channel)
    }

    fun createMainServiceFGSNotification(isPaused: Boolean): Notification {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_IMMUTABLE
        )

        // PendingIntent to STOP the service
        val stopIntent = Intent(context, MainService::class.java).apply { action = ACTION_STOP }
        val stopPendingIntent = PendingIntent.getService(
            context, 1, stopIntent, PendingIntent.FLAG_IMMUTABLE
        )

        // PendingIntent to PAUSE the service
        val pauseIntent = Intent(context, MainService::class.java).apply { action = ACTION_PAUSE }
        val pausePendingIntent = PendingIntent.getService(
            context, 2, pauseIntent, PendingIntent.FLAG_IMMUTABLE
        )

        val pauseResumeActionText = context.getString(
            when (isPaused) {
                true -> R.string.resume
                false -> R.string.pause
            }
        )

        val setContentText = context.getString(
            when (isPaused) {
                true -> R.string.service_disabled_desc
                false -> R.string.service_enabled_desc
            }
        )

        val setContentTitle = context.getString(
            when (isPaused) {
                true -> R.string.service_disabled_title
                false -> R.string.service_enabled_title
            }
        )

        val pauseResumeIcon = if (isPaused) android.R.drawable.ic_media_play else android.R.drawable.ic_media_pause

        return NotificationCompat.Builder(context, MAIN_CHANNEL_ID)
            .setContentTitle(setContentTitle)
            .setContentText(setContentText)
            .setSmallIcon(R.drawable.ic_notification)
            .setOngoing(true)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setCategory(NotificationCompat.CATEGORY_SERVICE)
            .setContentIntent(pendingIntent)
            .addAction(pauseResumeIcon, pauseResumeActionText, pausePendingIntent)
            .addAction(
                android.R.drawable.ic_menu_close_clear_cancel,
                context.getString(R.string.action_stop),
                stopPendingIntent
            )
            .build()
    }

    fun notifyMainServiceFGSNotification(isPaused: Boolean) {
        notificationManager.notify(FGS_NOTIFICATION_ID, createMainServiceFGSNotification(isPaused))
    }
}
