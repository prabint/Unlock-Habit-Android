package prabin.timsina.unlockhabit.ui.screens.home

import android.Manifest
import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.update
import prabin.timsina.unlockhabit.permissions.isDrawOverPermissionGranted
import prabin.timsina.unlockhabit.permissions.isPermissionGranted
import prabin.timsina.unlockhabit.repository.UserPreferencesRepository
import prabin.timsina.unlockhabit.services.MainService
import prabin.timsina.unlockhabit.services.ServiceTracker
import prabin.timsina.unlockhabit.ui.screens.app_picker.AppInfo
import prabin.timsina.unlockhabit.ui.screens.app_picker.InstalledAppRepository
import javax.inject.Inject

data class HomeScreenState(
    val isServiceRunning: Boolean = false,
    val isServicePaused: Boolean = false,
    val preferredApp: AppInfo? = null,
    val showRationaleDialog: Boolean = false,
)

sealed interface HomeScreenAction {
    data object OnClickToggleService : HomeScreenAction
    data object OnDismissRationalDialog : HomeScreenAction
}

@HiltViewModel
class HomeScreenViewModel @Inject constructor(
    @param:ApplicationContext private val context: Context,
    private val serviceTracker: ServiceTracker,
    userPreferencesRepository: UserPreferencesRepository,
    installedAppRepository: InstalledAppRepository,
) : ViewModel() {
    private val _uiState = MutableStateFlow(HomeScreenState())
    val uiState = _uiState.asStateFlow()

    init {
        combine(
            serviceTracker.isRunning,
            serviceTracker.isPaused,
            userPreferencesRepository.autoLaunchPackage,
        ) { isServiceRunning, isPaused, preferredPkg ->
            _uiState.update { state ->
                state.copy(
                    isServiceRunning = isServiceRunning,
                    isServicePaused = isPaused,
                    preferredApp = preferredPkg?.let { packageName ->
                        installedAppRepository.getAppInfo(
                            context = context,
                            packageName = packageName
                        )
                    },
                )
            }
        }.launchIn(viewModelScope)
    }

    fun onAction(action: HomeScreenAction) {
        when (action) {
            HomeScreenAction.OnClickToggleService -> {
                if (!isPermissionGranted(context, Manifest.permission.POST_NOTIFICATIONS)
                    || !isDrawOverPermissionGranted(context)
                ) {
                    _uiState.update { it.copy(showRationaleDialog = true) }
                    return
                }

                if (serviceTracker.isRunning.value) {
                    MainService.stopService(context)
                } else {
                    MainService.startService(context)
                }
            }

            HomeScreenAction.OnDismissRationalDialog -> {
                _uiState.update { it.copy(showRationaleDialog = false) }
            }
        }
    }
}
