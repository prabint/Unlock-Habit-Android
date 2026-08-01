package prabin.timsina.unlockhabit.ui.screens.overlay.slider

import android.content.Context
import android.graphics.PixelFormat
import android.view.WindowManager
import androidx.compose.ui.platform.ComposeView
import androidx.lifecycle.setViewTreeLifecycleOwner
import androidx.lifecycle.setViewTreeViewModelStoreOwner
import androidx.savedstate.setViewTreeSavedStateRegistryOwner
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import prabin.timsina.unlockhabit.broadcasts.launchApp
import prabin.timsina.unlockhabit.repository.UserPreferencesRepository
import prabin.timsina.unlockhabit.repository.models.TodayYesterdayCounts
import prabin.timsina.unlockhabit.ui.screens.app_picker.AppInfo
import prabin.timsina.unlockhabit.ui.screens.overlay.OverlayLifecycleOwner
import prabin.timsina.unlockhabit.ui.theme.AppTheme
import prabin.timsina.unlockhabit.utils.ApplicationScope
import javax.inject.Inject
import javax.inject.Singleton

sealed interface TextOverlayAction {
    data object OpenSettings : TextOverlayAction
    data object OpenApp : TextOverlayAction
    data object Skip : TextOverlayAction
}

@Singleton
class SliderOverlayController @Inject constructor(
    @param:ApplicationContext private val context: Context,
    private val userPreferencesRepository: UserPreferencesRepository,
    @ApplicationScope private val scope: CoroutineScope,
) {
    private val windowManager = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
    private var composeView: ComposeView? = null
    private var lifecycleOwner: OverlayLifecycleOwner? = null
    private lateinit var appInfo: AppInfo

    fun show(
        appInfo: AppInfo,
        heartsLeft: Int,
        todayYesterdayCounts: TodayYesterdayCounts,
    ) {
        remove()

        this.appInfo = appInfo

        val owner = OverlayLifecycleOwner()
        val view = ComposeView(context).apply {
            setViewTreeLifecycleOwner(owner)
            setViewTreeViewModelStoreOwner(owner)
            setViewTreeSavedStateRegistryOwner(owner)

            setContent {
                AppTheme { // Needed to support dark mode
                    SliderOverlayScreen(
                        preferredAppName = appInfo.name,
                        heartsLeft = heartsLeft,
                        todayYesterdayCounts = todayYesterdayCounts,
                        onAction = ::onAction,
                    )
                }
            }
        }

        val layoutParams = WindowManager.LayoutParams(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
            PixelFormat.TRANSLUCENT
        )

        this.lifecycleOwner = owner
        this.composeView = view

        owner.onCreate()
        windowManager.addView(view, layoutParams)
    }

    fun onAction(action: TextOverlayAction) {
        when (action) {
            TextOverlayAction.OpenApp -> {
                scope.launch {
                    userPreferencesRepository.addDailyUnlock()
                    withContext(Dispatchers.Main) {
                        launchApp(context, appInfo.packageName)
                        remove()
                    }
                }
            }

            TextOverlayAction.OpenSettings -> {
                scope.launch {
                    userPreferencesRepository.addDailyUnlock()
                    withContext(Dispatchers.Main) {
                        launchApp(context, context.packageName)
                        remove()
                    }
                }
            }

            TextOverlayAction.Skip -> {
                scope.launch {
                    userPreferencesRepository.decreaseHeartsLeft()
                    userPreferencesRepository.addDailyUnlock()
                    withContext(Dispatchers.Main) {
                        remove()
                    }
                }
            }
        }
    }

    fun remove() {
        val view = composeView ?: return
        val owner = lifecycleOwner

        composeView = null
        lifecycleOwner = null

        windowManager.removeView(view)
        owner?.onDestroy()
    }
}
