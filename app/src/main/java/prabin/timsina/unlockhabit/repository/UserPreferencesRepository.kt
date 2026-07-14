package prabin.timsina.unlockhabit.repository

import kotlinx.coroutines.flow.Flow

interface UserPreferencesRepository {
    val autoLaunchPackage: Flow<String?>
    suspend fun setAutoLaunchPackage(packageName: String)
}
