package prabin.timsina.unlockhabit.ui.screens.overlay

import android.content.Context
import android.view.ContextThemeWrapper
import android.view.Gravity
import android.view.WindowManager
import dagger.hilt.android.qualifiers.ApplicationContext
import prabin.timsina.unlockhabit.R
import prabin.timsina.unlockhabit.broadcasts.launchApp
import prabin.timsina.unlockhabit.ui.screens.app_picker.AppInfo
import timber.log.Timber
import javax.inject.Inject
import kotlin.math.roundToInt

class OverlayController @Inject constructor(
    @param:ApplicationContext private val context: Context,
) {
    private val windowManager = context.getSystemService(WindowManager::class.java)
    private val layoutParams = WindowManager.LayoutParams(
        (windowManager.currentWindowMetrics.bounds.width() * 0.8f).roundToInt(),
        WindowManager.LayoutParams.WRAP_CONTENT,
        WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
        WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN or WindowManager.LayoutParams.FLAG_DIM_BEHIND,
        android.graphics.PixelFormat.TRANSLUCENT
    ).apply {
        dimAmount = 0.5f
        gravity = Gravity.CENTER
    }

    private var overlayScreen: OverlayScreen? = null

    fun showView(appInfo: AppInfo) {
        if (overlayScreen != null) return

        try {
            val screen = OverlayScreen(
                context = ContextThemeWrapper(context, R.style.Theme_LockScreenReminder),
                onClose = { removeView() },
                onOpen = { appInfo ->
                    launchApp(context, appInfo.packageName)
                    removeView()
                }
            )
            screen.bind(appInfo)
            windowManager.addView(screen.root, layoutParams)
            overlayScreen = screen
        } catch (e: Exception) {
            Timber.e(e, "Error showing overlay view")
        }
    }

    fun removeView() {
        if (overlayScreen == null) return
        try {
            overlayScreen?.let { windowManager.removeView(it.root) }
        } catch (e: Exception) {
            Timber.e(e, "Error removing overlay view")
        } finally {
            overlayScreen = null
        }
    }
}