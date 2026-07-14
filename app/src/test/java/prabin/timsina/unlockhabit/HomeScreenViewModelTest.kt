package prabin.timsina.unlockhabit

import android.content.Context
import app.cash.turbine.test
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import prabin.timsina.unlockhabit.repository.UserPreferencesRepository
import prabin.timsina.unlockhabit.services.ServiceTracker
import prabin.timsina.unlockhabit.ui.screens.app_picker.AppInfo
import prabin.timsina.unlockhabit.ui.screens.app_picker.InstalledAppRepository
import prabin.timsina.unlockhabit.ui.screens.home.HomeScreenViewModel

@OptIn(ExperimentalCoroutinesApi::class)
class HomeScreenViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private val context: Context = mockk()
    private val serviceTracker: ServiceTracker = mockk()
    private val userPreferencesRepository: UserPreferencesRepository = mockk()
    private val installedAppRepository: InstalledAppRepository = mockk()

    // flows used to emit values to the ViewModel
    private val isRunningFlow = MutableStateFlow(false)
    private val isPausedFlow = MutableStateFlow(false)
    private val preferredPkgFlow = MutableStateFlow<String?>(null)

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)

        // Setup the flows that the ViewModel observes
        every { serviceTracker.isRunning } returns isRunningFlow
        every { serviceTracker.isPaused } returns isPausedFlow
        every { userPreferencesRepository.autoLaunchPackage } returns preferredPkgFlow
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `uiState updates when service state changes`() = runTest {
        val viewModel = HomeScreenViewModel(
            context,
            serviceTracker,
            userPreferencesRepository,
            installedAppRepository
        )

        viewModel.uiState.test {
            // Initial state (emitted immediately upon collection)
            val initialState = awaitItem()
            assertFalse(initialState.isServiceRunning)
            assertFalse(initialState.isServicePaused)

            // Update service running state
            isRunningFlow.value = true
            assertTrue(awaitItem().isServiceRunning)

            // Update service paused state
            isPausedFlow.value = true
            val pausedState = awaitItem()
            assertTrue(pausedState.isServiceRunning)
            assertTrue(pausedState.isServicePaused)
        }
    }

    @Test
    fun `uiState updates when preferred app changes`() = runTest {
        val testPackage = "com.example.app"
        val expectedApp = AppInfo("Test App", testPackage, mockk())

        // Stub the repository to return our mock app info
        every {
            installedAppRepository.getAppInfo(context, testPackage)
        } returns expectedApp

        val viewModel = HomeScreenViewModel(
            context,
            serviceTracker,
            userPreferencesRepository,
            installedAppRepository
        )

        viewModel.uiState.test {
            // Initial state has no app
            assertEquals(null, awaitItem().preferredApp)

            // Simulate preferred package changing in DataStore
            preferredPkgFlow.value = testPackage

            // Verify the UI state now contains the mapped AppInfo
            assertEquals(expectedApp, awaitItem().preferredApp)
        }
    }

    @Test
    fun `uiState shows null preferredApp when package is not found`() = runTest {
        val testPackage = "com.example.app"
        val unknownPackage = "com.unknown.app"
        val testApp = AppInfo("Test App", testPackage, mockk())

        // Mock returns an app for the first package, but null for the second
        every { installedAppRepository.getAppInfo(context, testPackage) } returns testApp
        every { installedAppRepository.getAppInfo(context, unknownPackage) } returns null

        val viewModel = HomeScreenViewModel(
            context,
            serviceTracker,
            userPreferencesRepository,
            installedAppRepository
        )

        viewModel.uiState.test {
            assertEquals(null, awaitItem().preferredApp) // Initial state

            // 1. Transition from null to Test App
            preferredPkgFlow.value = testPackage
            assertEquals(testApp, awaitItem().preferredApp)

            // 2. Transition from Test App back to null (simulating package removed/not found)
            preferredPkgFlow.value = unknownPackage
            assertEquals(null, awaitItem().preferredApp)
        }
    }
}
