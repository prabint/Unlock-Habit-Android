package prabin.timsina.unlockhabit.ui.screens.overlay.slider

import android.content.Context
import android.view.WindowManager
import androidx.compose.ui.platform.ComposeView
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.unmockkAll
import io.mockk.verify
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import prabin.timsina.unlockhabit.broadcasts.launchApp
import prabin.timsina.unlockhabit.repository.UserPreferencesRepository
import prabin.timsina.unlockhabit.ui.screens.app_picker.AppInfo
import prabin.timsina.unlockhabit.ui.screens.overlay.OverlayLifecycleOwner

@OptIn(ExperimentalCoroutinesApi::class)
class SliderOverlayControllerTest {

    private val context: Context = mockk(relaxed = true)
    private val windowManager: WindowManager = mockk(relaxed = true)
    private val userPreferencesRepository: UserPreferencesRepository = mockk(relaxed = true)
    private val testDispatcher = StandardTestDispatcher()
    private val testScope = TestScope(testDispatcher)

    private lateinit var controller: SliderOverlayController

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        every { context.getSystemService(Context.WINDOW_SERVICE) } returns windowManager
        every { context.packageName } returns "prabin.timsina.unlockhabit"
        
        mockkStatic("prabin.timsina.unlockhabit.broadcasts.ScreenUnlockReceiverKt")
        every { launchApp(any(), any()) } returns Unit

        controller = SliderOverlayController(
            context = context,
            userPreferencesRepository = userPreferencesRepository,
            scope = testScope,
        )
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
        unmockkAll()
    }

    @Test
    fun `remove does nothing when composeView is null`() {
        controller.remove()
        verify(exactly = 0) { windowManager.removeView(any()) }
    }

    @Test
    fun `remove clears views and destroys lifecycle owner when composeView is present`() {
        val mockView: ComposeView = mockk(relaxed = true)
        val mockOwner: OverlayLifecycleOwner = mockk(relaxed = true)

        // Use reflection to set private fields
        setPrivateField(controller, "composeView", mockView)
        setPrivateField(controller, "lifecycleOwner", mockOwner)

        controller.remove()

        verify(exactly = 1) { windowManager.removeView(mockView) }
        verify(exactly = 1) { mockOwner.onDestroy() }

        assertNull(getPrivateField(controller, "composeView"))
        assertNull(getPrivateField(controller, "lifecycleOwner"))
    }

    @Test
    fun `onAction OpenApp adds daily unlock, launches app, and removes overlay`() = runTest(testDispatcher) {
        val mockView: ComposeView = mockk(relaxed = true)
        val mockAppInfo = AppInfo("Target App", "com.target.app", mockk())
        
        setPrivateField(controller, "composeView", mockView)
        setPrivateField(controller, "appInfo", mockAppInfo)

        coEvery { userPreferencesRepository.addDailyUnlock() } returns Unit

        controller.onAction(TextOverlayAction.OpenApp)
        advanceUntilIdle()

        coVerify(exactly = 1) { userPreferencesRepository.addDailyUnlock() }
        verify(exactly = 1) { launchApp(context, "com.target.app") }
        verify(exactly = 1) { windowManager.removeView(mockView) }
        assertNull(getPrivateField(controller, "composeView"))
    }

    @Test
    fun `onAction OpenSettings adds daily unlock, launches settings app, and removes overlay`() = runTest(testDispatcher) {
        val mockView: ComposeView = mockk(relaxed = true)
        
        setPrivateField(controller, "composeView", mockView)

        coEvery { userPreferencesRepository.addDailyUnlock() } returns Unit

        controller.onAction(TextOverlayAction.OpenSettings)
        advanceUntilIdle()

        coVerify(exactly = 1) { userPreferencesRepository.addDailyUnlock() }
        verify(exactly = 1) { launchApp(context, "prabin.timsina.unlockhabit") }
        verify(exactly = 1) { windowManager.removeView(mockView) }
        assertNull(getPrivateField(controller, "composeView"))
    }

    @Test
    fun `onAction Skip decreases hearts, adds daily unlock, and removes overlay`() = runTest(testDispatcher) {
        val mockView: ComposeView = mockk(relaxed = true)
        
        setPrivateField(controller, "composeView", mockView)

        coEvery { userPreferencesRepository.decreaseHeartsLeft() } returns Unit
        coEvery { userPreferencesRepository.addDailyUnlock() } returns Unit

        controller.onAction(TextOverlayAction.Skip)
        advanceUntilIdle()

        coVerify(exactly = 1) { userPreferencesRepository.decreaseHeartsLeft() }
        coVerify(exactly = 1) { userPreferencesRepository.addDailyUnlock() }
        verify(exactly = 1) { windowManager.removeView(mockView) }
        assertNull(getPrivateField(controller, "composeView"))
    }

    private fun setPrivateField(obj: Any, fieldName: String, value: Any?) {
        val field = obj.javaClass.getDeclaredField(fieldName)
        field.isAccessible = true
        field.set(obj, value)
    }

    private fun getPrivateField(obj: Any, fieldName: String): Any? {
        val field = obj.javaClass.getDeclaredField(fieldName)
        field.isAccessible = true
        return field.get(obj)
    }
}
