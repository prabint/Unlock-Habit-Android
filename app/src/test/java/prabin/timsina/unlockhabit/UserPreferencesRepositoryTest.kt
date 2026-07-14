package prabin.timsina.unlockhabit

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import app.cash.turbine.test
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runTest
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import prabin.timsina.unlockhabit.repository.DefaultUserPreferencesRepository
import prabin.timsina.unlockhabit.repository.UserPreferencesRepository
import java.io.File

class UserPreferencesRepositoryTest {
    private lateinit var dataStore: DataStore<Preferences>
    private lateinit var repository: UserPreferencesRepository
    private val testScope = TestScope(StandardTestDispatcher())

    @Before
    fun setup() {
        // Create an in-memory DataStore for testing
        dataStore = PreferenceDataStoreFactory.create(
            scope = testScope,
            produceFile = { File.createTempFile("test_datastore", ".preferences_pb") }
        )
        repository = DefaultUserPreferencesRepository(dataStore)
    }

    @Test
    fun `autoLaunchPackage emits null by default`() = testScope.runTest {
        repository.autoLaunchPackage.test {
            Assert.assertEquals(null, awaitItem())
        }
    }

    @Test
    fun `setAutoLaunchPackage updates the flow with new package`() = testScope.runTest {
        val testPackage = "com.example.app"
        repository.setAutoLaunchPackage(testPackage)

        repository.autoLaunchPackage.test {
            Assert.assertEquals(testPackage, awaitItem())
        }
    }
}