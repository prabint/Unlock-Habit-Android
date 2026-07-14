package prabin.timsina.unlockhabit.repository

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DefaultUserPreferencesRepository @Inject constructor(
    private val dataStore: DataStore<Preferences>,
) : UserPreferencesRepository {
    companion object {
        private val KEY_AUTO_LAUNCH_PACKAGE = stringPreferencesKey("auto_launch_app")
    }

    override val autoLaunchPackage: Flow<String?> = dataStore.data
        .catch { exception ->
            if (exception is IOException) {
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }
        .map { preferences ->
            preferences[KEY_AUTO_LAUNCH_PACKAGE]
        }

    override suspend fun setAutoLaunchPackage(packageName: String) {
        dataStore.edit { preferences ->
            preferences[KEY_AUTO_LAUNCH_PACKAGE] = packageName
        }
    }
}