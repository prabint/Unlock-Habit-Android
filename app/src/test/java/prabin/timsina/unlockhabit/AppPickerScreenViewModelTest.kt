package prabin.timsina.unlockhabit

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import app.cash.turbine.test
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import prabin.timsina.unlockhabit.repository.UserPreferencesRepository
import prabin.timsina.unlockhabit.ui.screens.app_picker.AppInfo
import prabin.timsina.unlockhabit.ui.screens.app_picker.AppPickerAction
import prabin.timsina.unlockhabit.ui.screens.app_picker.AppPickerScreenViewModel
import prabin.timsina.unlockhabit.ui.screens.app_picker.InstalledAppRepository

@OptIn(ExperimentalCoroutinesApi::class)
class AppPickerScreenViewModelTest {
    private val testDispatcher = StandardTestDispatcher()
    private val context: Context = mockk()
    private val packageManager: PackageManager = mockk()
    private val userPreferencesRepository: UserPreferencesRepository = mockk()
    private val installedAppRepository: InstalledAppRepository = mockk()

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        every { context.packageManager } returns packageManager

        // Default: no apps found during init
        every { packageManager.queryIntentActivities(any<Intent>(), 0) } returns emptyList()
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `initial state loads apps from repository`() = runTest {
        val sortedApps = listOf(
            AppInfo("A App", "com.a.app", mockk()),
            AppInfo("B App", "com.b.app", mockk())
        )
        coEvery { installedAppRepository.getInstalledLauncherApps(any()) } returns sortedApps

        val viewModel = AppPickerScreenViewModel(context, userPreferencesRepository, installedAppRepository)
        advanceUntilIdle()

        assertEquals(sortedApps, viewModel.uiState.value.allApps)
    }

    @Test
    fun `OnAppSelected action saves to repository and triggers navigation`() = runTest {
        val testApp = AppInfo("Test", "com.test", mockk())
        coEvery { userPreferencesRepository.setAutoLaunchPackage(any()) } returns Unit
        coEvery { installedAppRepository.getInstalledLauncherApps(any()) } returns listOf(testApp)

        val viewModel = AppPickerScreenViewModel(context, userPreferencesRepository, installedAppRepository)

        viewModel.navigationEvent.test {
            viewModel.onAction(AppPickerAction.OnAppSelected(testApp))
            advanceUntilIdle()
            awaitItem() // Verify navigation event received
            coVerify { userPreferencesRepository.setAutoLaunchPackage("com.test") }
        }
    }
}