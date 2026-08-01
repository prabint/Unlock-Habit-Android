package prabin.timsina.unlockhabit.repository

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.serialization.json.Json
import prabin.timsina.unlockhabit.repository.models.DailyUnlock
import prabin.timsina.unlockhabit.repository.models.TodayYesterdayCounts
import prabin.timsina.unlockhabit.repository.models.UserPreferences
import java.io.IOException
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DefaultUserPreferencesRepository @Inject constructor(
    private val dataStore: DataStore<Preferences>,
    private val json: Json,
) : UserPreferencesRepository {
    companion object {
        private val KEY_AUTO_LAUNCH_PACKAGE = stringPreferencesKey("auto_launch_app")
        private val KEY_USER_ENABLED_SERVICE = booleanPreferencesKey("user_enabled_service")
        private val KEY_SHOULD_LAUNCH_DIRECTLY = booleanPreferencesKey("should_launch_directly")
        private val KEY_HEARTS_LEFT = intPreferencesKey("hearts_left")
        private val KEY_HEARTS_RESET_AT = longPreferencesKey("hearts_reset_at")
        val KEY_DAILY_UNLOCKS = stringPreferencesKey("daily_unlocks")
        const val MAX_HEARTS = 5
    }

    private val dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")

    override val preferences: Flow<UserPreferences> = dataStore.data
        .catch { exception ->
            if (exception is IOException) {
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }.map { prefs ->
            UserPreferences(
                autoLaunchPackage = prefs[KEY_AUTO_LAUNCH_PACKAGE],
                userEnabledService = prefs[KEY_USER_ENABLED_SERVICE] ?: false,
                shouldLaunchDirectly = prefs[KEY_SHOULD_LAUNCH_DIRECTLY] ?: true,
                heartsLeft = prefs[KEY_HEARTS_LEFT] ?: MAX_HEARTS,
                heartsResetAt = prefs[KEY_HEARTS_RESET_AT] ?: 0L,
                dailyUnlocks = prefs[KEY_DAILY_UNLOCKS]?.let {
                    runCatching { json.decodeFromString<List<DailyUnlock>>(it) }.getOrDefault(emptyList())
                } ?: emptyList(),
            )
        }

    override suspend fun addDailyUnlock() {
        dataStore.edit { preferences ->
            val dailyUnlocksJson = preferences[KEY_DAILY_UNLOCKS]
            val currentList = when {
                dailyUnlocksJson != null -> runCatching {
                    json.decodeFromString<List<DailyUnlock>>(dailyUnlocksJson)
                }.getOrDefault(emptyList()).toMutableList()

                else -> mutableListOf()
            }
            val today = LocalDate.now().format(dateFormatter)
            val index = currentList.indexOfFirst { it.date == today }
            if (index == -1) {
                currentList.add(0, DailyUnlock(today, 1))
            } else {
                currentList[index] = currentList[index].copy(count = currentList[index].count + 1)
            }
            preferences[KEY_DAILY_UNLOCKS] = json.encodeToString(currentList)
        }
    }

    override suspend fun setHeartsLeft(count: Int) {
        dataStore.edit { preferences ->
            preferences[KEY_HEARTS_LEFT] = count
        }
    }

    override suspend fun getTodayAndYesterdayUnlockCount(): TodayYesterdayCounts {
        val dailyUnlocks = preferences.first().dailyUnlocks

        val yesterday = LocalDate.now().minusDays(1).format(dateFormatter)
        val yesterdayUnlockCount = dailyUnlocks.firstOrNull { it.date == yesterday }?.count ?: 0

        val today = LocalDate.now().format(dateFormatter)
        val todayUnlockCount = dailyUnlocks.firstOrNull { it.date == today }?.count ?: 0

        return TodayYesterdayCounts(
            today = todayUnlockCount,
            yesterday = yesterdayUnlockCount
        )
    }

    override suspend fun decreaseHeartsLeft() {
        dataStore.edit { preferences ->
            val cur = preferences[KEY_HEARTS_LEFT] ?: MAX_HEARTS
            if (cur > 0) {
                preferences[KEY_HEARTS_LEFT] = cur - 1
            }
        }
    }

    override suspend fun setHeartsResetAt(timeInMillis: Long) {
        dataStore.edit { preferences ->
            preferences[KEY_HEARTS_RESET_AT] = timeInMillis
        }
    }

    override suspend fun setShouldLaunchDirectly(autoLaunch: Boolean) {
        dataStore.edit { preferences ->
            preferences[KEY_SHOULD_LAUNCH_DIRECTLY] = autoLaunch
        }
    }

    override suspend fun setAutoLaunchPackage(packageName: String) {
        dataStore.edit { preferences ->
            preferences[KEY_AUTO_LAUNCH_PACKAGE] = packageName
        }
    }

    override suspend fun setUserEnabledService(enabled: Boolean) {
        dataStore.edit { preferences ->
            preferences[KEY_USER_ENABLED_SERVICE] = enabled
        }
    }
}
