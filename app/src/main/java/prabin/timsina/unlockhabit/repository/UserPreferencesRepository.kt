package prabin.timsina.unlockhabit.repository

import kotlinx.coroutines.flow.Flow
import prabin.timsina.unlockhabit.repository.models.TodayYesterdayCounts
import prabin.timsina.unlockhabit.repository.models.UserPreferences

interface UserPreferencesRepository {
    val preferences: Flow<UserPreferences>

    suspend fun setHeartsLeft(count: Int)
    suspend fun decreaseHeartsLeft()
    suspend fun setHeartsResetAt(timeInMillis: Long)
    suspend fun setShouldLaunchDirectly(autoLaunch: Boolean)
    suspend fun setAutoLaunchPackage(packageName: String)
    suspend fun setUserEnabledService(enabled: Boolean)
    suspend fun addDailyUnlock()
    suspend fun getTodayAndYesterdayUnlockCount(): TodayYesterdayCounts
}
