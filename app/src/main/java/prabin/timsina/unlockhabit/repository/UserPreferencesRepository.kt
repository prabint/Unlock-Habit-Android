package prabin.timsina.unlockhabit.repository

import kotlinx.coroutines.flow.Flow

interface UserPreferencesRepository {
    val autoLaunchPackage: Flow<String?>
    val userEnabledService: Flow<Boolean>
    val shouldLaunchDirectly: Flow<Boolean>
    suspend fun setShouldLaunchDirectly(autoLaunch: Boolean)
    suspend fun setAutoLaunchPackage(packageName: String)
    suspend fun setUserEnabledService(enabled: Boolean)
}
