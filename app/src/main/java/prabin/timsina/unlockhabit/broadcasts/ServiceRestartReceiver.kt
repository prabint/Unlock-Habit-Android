package prabin.timsina.unlockhabit.broadcasts

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import prabin.timsina.unlockhabit.repository.UserPreferencesRepository
import prabin.timsina.unlockhabit.services.MainService
import prabin.timsina.unlockhabit.utils.ApplicationScope
import javax.inject.Inject

@AndroidEntryPoint
class ServiceRestartReceiver : BroadcastReceiver() {
    @Inject
    lateinit var repository: UserPreferencesRepository

    @Inject
    @ApplicationScope
    lateinit var scope: CoroutineScope

    override fun onReceive(context: Context, intent: Intent?) {
        if (intent?.action == Intent.ACTION_BOOT_COMPLETED ||
            intent?.action == Intent.ACTION_MY_PACKAGE_REPLACED
        ) {
            val pendingResult = goAsync()
            scope.launch {
                try {
                    if (repository.userEnabledService.first()) {
                        MainService.startService(context)
                    }
                } finally {
                    pendingResult.finish()
                }
            }
        }
    }
}
