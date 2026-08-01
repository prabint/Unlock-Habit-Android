package prabin.timsina.unlockhabit

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import app.cash.turbine.test
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.Json
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import prabin.timsina.unlockhabit.repository.DefaultUserPreferencesRepository
import prabin.timsina.unlockhabit.repository.DefaultUserPreferencesRepository.Companion.KEY_DAILY_UNLOCKS
import prabin.timsina.unlockhabit.repository.UserPreferencesRepository
import prabin.timsina.unlockhabit.repository.models.DailyUnlock
import java.io.File
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class UserPreferencesRepositoryTest {
    private lateinit var dataStore: DataStore<Preferences>
    private lateinit var repository: UserPreferencesRepository
    private val testScope = TestScope(StandardTestDispatcher())
    private val json = Json { ignoreUnknownKeys = true }
    private val dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")

    @Before
    fun setup() {
        // Create an in-memory DataStore for testing
        dataStore = PreferenceDataStoreFactory.create(
            scope = testScope,
            produceFile = { File.createTempFile("test_datastore", ".preferences_pb") }
        )
        repository = DefaultUserPreferencesRepository(
            dataStore = dataStore,
            json = json
        )
    }

    @Test
    fun `preferences emits default values initially`() = testScope.runTest {
        repository.preferences.test {
            val prefs = awaitItem()
            Assert.assertEquals(null, prefs.autoLaunchPackage)
            Assert.assertEquals(false, prefs.userEnabledService)
            Assert.assertEquals(true, prefs.shouldLaunchDirectly)
            Assert.assertEquals(emptyList<DailyUnlock>(), prefs.dailyUnlocks)
        }
    }

    @Test
    fun `setAutoLaunchPackage updates the flow with new package`() = testScope.runTest {
        val testPackage = "com.example.app"
        repository.setAutoLaunchPackage(testPackage)

        repository.preferences.test {
            Assert.assertEquals(testPackage, awaitItem().autoLaunchPackage)
        }
    }

    @Test
    fun `setUserEnabledService updates the flow correctly`() = testScope.runTest {
        // Initial state
        repository.preferences.test {
            Assert.assertEquals(false, awaitItem().userEnabledService)

            // Update to true
            repository.setUserEnabledService(true)
            Assert.assertEquals(true, awaitItem().userEnabledService)

            // Update to false
            repository.setUserEnabledService(false)
            Assert.assertEquals(false, awaitItem().userEnabledService)
        }
    }

    @Test
    fun `setShouldLaunchDirectly updates the flow with new value`() = testScope.runTest {
        repository.setShouldLaunchDirectly(false)

        repository.preferences.test {
            Assert.assertEquals(false, awaitItem().shouldLaunchDirectly)
        }
    }

    @Test
    fun `dailyUnlocks emits empty list by default`() = testScope.runTest {
        repository.preferences.test {
            Assert.assertEquals(emptyList<DailyUnlock>(), awaitItem().dailyUnlocks)
        }
    }

    @Test
    fun `addDailyUnlock adds new entry for today if not exists`() = testScope.runTest {
        val today = LocalDate.now().format(dateFormatter)

        repository.addDailyUnlock()

        repository.preferences.test {
            val list = awaitItem().dailyUnlocks
            Assert.assertEquals(1, list.size)
            Assert.assertEquals(today, list[0].date)
            Assert.assertEquals(1, list[0].count)
        }
    }

    @Test
    fun `addDailyUnlock increments count for today if already exists`() = testScope.runTest {
        val today = LocalDate.now().format(dateFormatter)

        repository.addDailyUnlock()
        repository.addDailyUnlock()

        repository.preferences.test {
            val list = awaitItem().dailyUnlocks
            Assert.assertEquals(1, list.size)
            Assert.assertEquals(today, list[0].date)
            Assert.assertEquals(2, list[0].count)
        }
    }

    @Test
    fun `getTodayAndYesterdayUnlockCount returns correct counts when data exists`() = testScope.runTest {
        val today = LocalDate.now().format(dateFormatter)
        val yesterday = LocalDate.now().minusDays(1).format(dateFormatter)

        // Manually seed dataStore for yesterday and today
        val initialData = listOf(
            DailyUnlock(today, 5),
            DailyUnlock(yesterday, 3)
        )
        dataStore.edit { preferences ->
            preferences[KEY_DAILY_UNLOCKS] = json.encodeToString(initialData)
        }

        val counts = repository.getTodayAndYesterdayUnlockCount()

        Assert.assertEquals(5, counts.today)
        Assert.assertEquals(3, counts.yesterday)
    }

    @Test
    fun `getTodayAndYesterdayUnlockCount returns zeros when no data exists`() = testScope.runTest {
        val counts = repository.getTodayAndYesterdayUnlockCount()

        Assert.assertEquals(0, counts.today)
        Assert.assertEquals(0, counts.yesterday)
    }

    @Test
    fun `dailyUnlocks emits empty list if data is corrupted`() = testScope.runTest {
        dataStore.edit { preferences ->
            preferences[KEY_DAILY_UNLOCKS] = "corrupted-json"
        }

        repository.preferences.test {
            Assert.assertEquals(emptyList<DailyUnlock>(), awaitItem().dailyUnlocks)
        }
    }

    @Test
    fun `addDailyUnlock resets list if data is corrupted`() = testScope.runTest {
        dataStore.edit { preferences ->
            preferences[KEY_DAILY_UNLOCKS] = "{ invalid json }"
        }

        repository.addDailyUnlock()

        repository.preferences.test {
            val list = awaitItem().dailyUnlocks
            Assert.assertEquals(1, list.size)
            Assert.assertEquals(1, list[0].count)
        }
    }
}
