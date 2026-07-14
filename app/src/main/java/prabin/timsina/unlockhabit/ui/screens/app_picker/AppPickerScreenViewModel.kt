package prabin.timsina.unlockhabit.ui.screens.app_picker

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import prabin.timsina.unlockhabit.repository.UserPreferencesRepository
import javax.inject.Inject

data class AppPickerScreenState(
    val allApps: List<AppInfo> = emptyList(),
    val isLoading: Boolean = false,
)

sealed interface AppPickerAction {
    data class OnAppSelected(val appInfo: AppInfo) : AppPickerAction
}

@HiltViewModel
class AppPickerScreenViewModel @Inject constructor(
    @param:ApplicationContext private val context: Context,
    private val userPreferencesRepository: UserPreferencesRepository,
    installedAppRepository: InstalledAppRepository,
) : ViewModel() {
    private val _uiState = MutableStateFlow(AppPickerScreenState())
    internal val uiState = _uiState.asStateFlow()

    private val _navigationEvent = Channel<Unit>()
    internal val navigationEvent = _navigationEvent.receiveAsFlow()

    init {
        _uiState.update { it.copy(isLoading = true) }
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isLoading = false,
                allApps = installedAppRepository.getInstalledLauncherApps(context)
            )
        }
    }

    fun onAction(action: AppPickerAction) {
        when (action) {
            is AppPickerAction.OnAppSelected -> {
                viewModelScope.launch {
                    userPreferencesRepository.setAutoLaunchPackage(action.appInfo.packageName)
                    _navigationEvent.send(Unit)
                }
            }
        }
    }
}

