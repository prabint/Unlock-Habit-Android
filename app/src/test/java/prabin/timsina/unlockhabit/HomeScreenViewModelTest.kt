package prabin.timsina.unlockhabit

import android.content.Context
import app.cash.turbine.test
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.mockkStatic
import io.mockk.unmockkObject
import io.mockk.unmockkStatic
import io.mockk.verify
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import prabin.timsina.unlockhabit.permissions.isDrawOverPermissionGranted
import prabin.timsina.unlockhabit.permissions.isPermissionGranted
import prabin.timsina.unlockhabit.repository.UserPreferencesRepository
import prabin.timsina.unlockhabit.services.MainService
import prabin.timsina.unlockhabit.services.PausedTracker
import prabin.timsina.unlockhabit.ui.screens.app_picker.AppInfo
import prabin.timsina.unlockhabit.ui.screens.app_picker.InstalledAppRepository
import prabin.timsina.unlockhabit.ui.screens.home.HomeScreenAction
import prabin.timsina.unlockhabit.ui.screens.home.HomeScreenViewModel

@OptIn(ExperimentalCoroutinesApi::class)
class HomeScreenViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private val context: Context = mockk()
    private val pausedTracker: PausedTracker = mockk()
    private val userPreferencesRepository: UserPreferencesRepository = mockk()
    private val installedAppRepository: InstalledAppRepository = mockk()

    // flows used to emit values to the ViewModel
    private val isServiceEnabledFlow = MutableStateFlow(false)
    private val isPausedFlow = MutableStateFlow(false)
    private val preferredPkgFlow = MutableStateFlow<String?>(null)

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)

        // Stub context methods to avoid MockKException when MainService is called
        every { context.startForegroundService(any()) } returns null
        every { context.stopService(any()) } returns true

        // Setup the flows that the ViewModel observes
        every { pausedTracker.isPaused } returns isPausedFlow
        every { userPreferencesRepository.autoLaunchPackage } returns preferredPkgFlow
        every { userPreferencesRepository.userEnabledService } returns isServiceEnabledFlow
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `uiState updates when service state changes`() = runTest {
        val viewModel = HomeScreenViewModel(
            context = context,
            userPreferencesRepository = userPreferencesRepository,
            pausedTracker = pausedTracker,
            installedAppRepository = installedAppRepository
        )

        viewModel.uiState.test {
            // Initial state (emitted immediately upon collection)
            val initialState = awaitItem()
            assertFalse(initialState.isServiceRunning)
            assertFalse(initialState.isPaused)

            // Update service running state
            isServiceEnabledFlow.value = true
            assertTrue(awaitItem().isServiceRunning)

            // Update service paused state
            isPausedFlow.value = true
            val pausedState = awaitItem()
            assertTrue(pausedState.isServiceRunning)
            assertTrue(pausedState.isPaused)
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
            context = context,
            pausedTracker = pausedTracker,
            userPreferencesRepository = userPreferencesRepository,
            installedAppRepository = installedAppRepository
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
            context = context,
            pausedTracker = pausedTracker,
            userPreferencesRepository = userPreferencesRepository,
            installedAppRepository = installedAppRepository
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

    @Test
    fun `OnClickToggleService shows rationale dialog when permissions are missing`() = runTest {
        mockkStatic("prabin.timsina.unlockhabit.permissions.PermissionsUtilsKt")

        // Given: Permission is NOT granted
        every { isPermissionGranted(any(), any()) } returns false

        val viewModel = HomeScreenViewModel(
            context = context,
            pausedTracker = pausedTracker,
            userPreferencesRepository = userPreferencesRepository,
            installedAppRepository = installedAppRepository
        )

        viewModel.uiState.test {
            awaitItem() // initial state

            // When: Toggle action is triggered
            viewModel.onAction(HomeScreenAction.OnClickToggleService(true))

            // Then: showRationaleDialog becomes true
            val state = awaitItem()
            assertTrue(state.showRationaleDialog)
        }

        unmockkStatic("prabin.timsina.unlockhabit.permissions.PermissionsUtilsKt")
    }

    @Test
    fun `OnClickToggleService persists true when starting service`() = runTest {
        mockkStatic("prabin.timsina.unlockhabit.permissions.PermissionsUtilsKt")
        mockkObject(MainService)

        // Given: Permissions are granted and service is currently OFF
        every { isPermissionGranted(any(), any()) } returns true
        every { isDrawOverPermissionGranted(any()) } returns true
        every { MainService.startService(any()) } returns Unit
        coEvery { userPreferencesRepository.setUserEnabledService(any()) } returns Unit
        isServiceEnabledFlow.value = false
        isPausedFlow.value = false // VM starts service when NOT paused

        val viewModel = HomeScreenViewModel(
            context = context,
            pausedTracker = pausedTracker,
            userPreferencesRepository = userPreferencesRepository,
            installedAppRepository = installedAppRepository
        )

        // When: Toggle action is triggered
        viewModel.onAction(HomeScreenAction.OnClickToggleService(true))

        // Ensure the coroutine in viewModelScope finishes
        advanceUntilIdle()

        // Then: Verify it attempts to start the service and persist 'true'
        verify { MainService.startService(any()) }
        coVerify { userPreferencesRepository.setUserEnabledService(true) }

        unmockkStatic("prabin.timsina.unlockhabit.permissions.PermissionsUtilsKt")
        unmockkObject(MainService)
    }

    @Test
    fun `OnClickToggleService persists false when stopping service`() = runTest {
        mockkStatic("prabin.timsina.unlockhabit.permissions.PermissionsUtilsKt")
        mockkObject(MainService)

        // Given: Permissions are granted and service is currently ON
        every { isPermissionGranted(any(), any()) } returns true
        every { isDrawOverPermissionGranted(any()) } returns true
        every { MainService.stopService(any()) } returns Unit
        coEvery { userPreferencesRepository.setUserEnabledService(any()) } returns Unit
        isServiceEnabledFlow.value = true
        isPausedFlow.value = true // VM stops service when paused

        val viewModel = HomeScreenViewModel(
            context = context,
            pausedTracker = pausedTracker,
            userPreferencesRepository = userPreferencesRepository,
            installedAppRepository = installedAppRepository
        )

        // When: Toggle action is triggered
        viewModel.onAction(HomeScreenAction.OnClickToggleService(false))

        // Ensure the coroutine in viewModelScope finishes
        advanceUntilIdle()

        // Then: Verify it attempts to stop the service and persist 'false'
        verify { MainService.stopService(any()) }
        coVerify { userPreferencesRepository.setUserEnabledService(false) }

        unmockkStatic("prabin.timsina.unlockhabit.permissions.PermissionsUtilsKt")
        unmockkObject(MainService)
    }
}
