package prabin.timsina.unlockhabit.ui.screens.overlay

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import dagger.hilt.android.qualifiers.ApplicationContext
import prabin.timsina.unlockhabit.R
import prabin.timsina.unlockhabit.ui.screens.app_picker.AppInfo

class OverlayScreen(
    @param:ApplicationContext private val context: Context,
    private val onClose: () -> Unit,
    private val onOpen: (AppInfo) -> Unit,
) {
    val root: View = LayoutInflater.from(context).inflate(R.layout.overlay_view, null)

    private val closeButton: Button = root.findViewById(R.id.close)
    private val openButton: Button = root.findViewById(R.id.open)
    private val iconImageView: ImageView = root.findViewById(R.id.iconImageView)
    private val appName: TextView = root.findViewById(R.id.appName)
    private val preferredAppLayout: LinearLayout = root.findViewById(R.id.preferredAppLayout)

    private var currentAppInfo: AppInfo? = null

    init {
        preferredAppLayout.setOnClickListener {
            currentAppInfo?.let(onOpen)
        }
        openButton.setOnClickListener {
            currentAppInfo?.let(onOpen)
        }
        closeButton.setOnClickListener {
            onClose()
        }
    }

    fun bind(appInfo: AppInfo) {
        currentAppInfo = appInfo
        iconImageView.setImageDrawable(appInfo.icon)
        appName.text = appInfo.name
    }
}