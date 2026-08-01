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
import kotlinx.coroutines.launch
import prabin.timsina.unlockhabit.permissions.isDrawOverPermissionGranted
import prabin.timsina.unlockhabit.permissions.isPermissionGranted
import prabin.timsina.unlockhabit.repository.UserPreferencesRepository
import prabin.timsina.unlockhabit.services.MainService
import prabin.timsina.unlockhabit.services.PausedTracker
import prabin.timsina.unlockhabit.ui.screens.app_picker.AppInfo
import prabin.timsina.unlockhabit.ui.screens.app_picker.InstalledAppRepository
import javax.inject.Inject

data class HomeScreenState(
    val isServiceRunning: Boolean = false,
    val isPaused: Boolean = false,
    val preferredApp: AppInfo? = null,
    val showRationaleDialog: Boolean = false,
    val shouldLaunchDirectly: Boolean = true,
)

sealed interface HomeScreenAction {
    data class OnClickToggleService(val enable: Boolean) : HomeScreenAction
    data object OnDismissRationalDialog : HomeScreenAction
    data object OnClickLaunchDirectly : HomeScreenAction
    data object OnClickShowOverlay : HomeScreenAction
}

@HiltViewModel
class HomeScreenViewModel @Inject constructor(
    @param:ApplicationContext private val context: Context,
    private val userPreferencesRepository: UserPreferencesRepository,
    pausedTracker: PausedTracker,
    installedAppRepository: InstalledAppRepository,
) : ViewModel() {
    private val _uiState = MutableStateFlow(HomeScreenState())
    val uiState = _uiState.asStateFlow()

    init {
        combine(
            pausedTracker.isPaused,
            userPreferencesRepository.userEnabledService,
            userPreferencesRepository.autoLaunchPackage,
            userPreferencesRepository.shouldLaunchDirectly,
        ) { isPaused, isServiceRunning, preferredPkg, launchDirectly ->
            _uiState.update { state ->
                state.copy(
                    isPaused = isPaused,
                    isServiceRunning = isServiceRunning,
                    shouldLaunchDirectly = launchDirectly,
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
            is HomeScreenAction.OnClickToggleService -> {
                if (!isPermissionGranted(context, Manifest.permission.POST_NOTIFICATIONS)
                    || !isDrawOverPermissionGranted(context)
                ) {
                    _uiState.update { it.copy(showRationaleDialog = true) }
                    return
                }

                viewModelScope.launch {
                    userPreferencesRepository.setUserEnabledService(enabled = action.enable)
                }

                if (action.enable) {
                    MainService.startService(context)
                } else {
                    MainService.stopService(context)
                }
            }

            HomeScreenAction.OnDismissRationalDialog -> {
                _uiState.update { it.copy(showRationaleDialog = false) }
            }

            HomeScreenAction.OnClickLaunchDirectly -> {
                viewModelScope.launch {
                    userPreferencesRepository.setShouldLaunchDirectly(true)
                }
            }

            HomeScreenAction.OnClickShowOverlay -> {
                viewModelScope.launch {
                    userPreferencesRepository.setShouldLaunchDirectly(false)
                }
            }
        }
    }
}
