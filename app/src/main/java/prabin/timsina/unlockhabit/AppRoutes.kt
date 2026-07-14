package prabin.timsina.unlockhabit

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

sealed class AppRoutes {
    @Serializable
    data object HomeScreen : NavKey, AppRoutes()

    @Serializable
    data object AppPickerScreen : NavKey, AppRoutes()
}
