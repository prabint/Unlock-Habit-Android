package prabin.timsina.unlockhabit.repository

import kotlinx.coroutines.flow.Flow

interface UserPreferencesRepository {
    val autoLaunchPackage: Flow<String?>
    val userEnabledService: Flow<Boolean>
    suspend fun setAutoLaunchPackage(packageName: String)
    suspend fun setUserEnabledService(enabled: Boolean)
}
