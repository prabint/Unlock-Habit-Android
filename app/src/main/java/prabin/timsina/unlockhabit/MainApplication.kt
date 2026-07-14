package prabin.timsina.unlockhabit

import android.app.Application
import dagger.hilt.android.HiltAndroidApp
import prabin.timsina.unlockhabit.notifications.AppNotificationManager
import prabin.timsina.unlockhabit.utils.AppDebugTree
import timber.log.Timber.Forest.plant
import javax.inject.Inject

@HiltAndroidApp
class MainApplication : Application() {
    @Inject
    lateinit var appNotificationManager: AppNotificationManager

    override fun onCreate() {
        super.onCreate()
        setupTimber()
        appNotificationManager.initialize()
    }

    private fun setupTimber() {
        if (BuildConfig.DEBUG) {
            plant(AppDebugTree)
        }
    }
}
