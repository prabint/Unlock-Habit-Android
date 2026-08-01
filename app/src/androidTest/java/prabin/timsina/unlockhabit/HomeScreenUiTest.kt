package prabin.timsina.unlockhabit

import android.os.Build
import android.os.ParcelFileDescriptor
import androidx.compose.ui.test.isToggleable
import androidx.compose.ui.test.junit4.v2.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.uiautomator.By
import androidx.test.uiautomator.UiDevice
import androidx.test.uiautomator.Until
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class HomeScreenUiTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<MainActivity>()

    private val uiAutomation = InstrumentationRegistry.getInstrumentation().uiAutomation
    private val device = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation())

    @Before
    fun setup() {
        // Grant permissions via shell for UI testing
        val packageName = InstrumentationRegistry.getInstrumentation().targetContext.packageName

        // Grant Overlay permission
        uiAutomation.executeShellCommand("appops set $packageName SYSTEM_ALERT_WINDOW allow")

        // Grant Notification permission if needed
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            uiAutomation.executeShellCommand("pm grant $packageName android.permission.POST_NOTIFICATIONS")
        }
    }

    @Test
    fun fullAppFlowTest() {
        val appContext = InstrumentationRegistry.getInstrumentation().targetContext
        val selectAppText = appContext.getString(R.string.select_application)
        val changeAppText = appContext.getString(R.string.change_application)
        val directLaunchText = appContext.getString(R.string.launch_directly_title)
        val showReminderText = appContext.getString(R.string.show_reminder_first)

        // 1. App Selection
        // Kill calendar app first to ensure it's launched fresh
        uiAutomation.executeShellCommand("am force-stop $CALENDAR_APP_PACKAGE")
        Thread.sleep(1000)

        // Verify it's not in the foreground
        assertTrue(
            "Calendar app should not be in foreground at start",
            !isAppInForeground()
        )

        // Wait for Home screen and select app
        composeTestRule.waitUntil(timeoutMillis = 5000) {
            composeTestRule.onAllNodesWithText(selectAppText).fetchSemanticsNodes().isNotEmpty() ||
                composeTestRule.onAllNodesWithText(changeAppText).fetchSemanticsNodes().isNotEmpty()
        }

        if (composeTestRule.onAllNodesWithText(selectAppText).fetchSemanticsNodes().isNotEmpty()) {
            composeTestRule.onNodeWithText(selectAppText).performClick()
        } else {
            composeTestRule.onNodeWithText(changeAppText).performClick()
        }

        // Wait for app list and pick the target app
        composeTestRule.waitUntil(timeoutMillis = 10000) {
            composeTestRule.onAllNodesWithText(CALENDAR_APP_NAME).fetchSemanticsNodes().isNotEmpty()
        }
        composeTestRule.onNodeWithText(CALENDAR_APP_NAME).performClick()

        // 2. Service Toggle & Mode Visibility
        composeTestRule.onNodeWithText(CALENDAR_APP_NAME).assertExists()

        // Initially, launch modes should not be visible if service is off
        composeTestRule.onNodeWithText(directLaunchText).assertDoesNotExist()

        // Turn service ON
        composeTestRule.onNode(isToggleable()).performClick()

        // Now launch modes should appear
        composeTestRule.onNodeWithText(directLaunchText).assertExists()
        composeTestRule.onNodeWithText(showReminderText).assertExists()

        // 3. Test Direct Launch Mode
        composeTestRule.onNodeWithText(directLaunchText).performClick()

        simulateLockAndUnlock()

        // Verify the target app is launched directly
        verifyAppInForeground()

        // 4. Test Overlay Mode
        // Go back to our app (it might be in background now)
        val targetPackage = appContext.packageName
        uiAutomation.executeShellCommand("am start -n $targetPackage/prabin.timsina.unlockhabit.MainActivity")
        Thread.sleep(2000)

        // Kill target app again to ensure the overlay launch is fresh
        uiAutomation.executeShellCommand("am force-stop $CALENDAR_APP_PACKAGE")
        Thread.sleep(1000)
        assertTrue(
            "Target app should not be in foreground before overlay test",
            !isAppInForeground()
        )

        composeTestRule.onNodeWithText(showReminderText).performClick()

        simulateLockAndUnlock()

        // Verify Overlay appears
        val overlayTitle = device.wait(Until.findObject(By.text(appContext.getString(R.string.app_name))), 5000)
        assertNotNull("Overlay should be visible", overlayTitle)

        val openButton = device.findObject(By.res("$targetPackage:id/open"))
        assertNotNull("Open button should be visible on overlay", openButton)

        // Click Open on Overlay
        openButton.click()

        // Verify the target app is launched after clicking Open
        verifyAppInForeground()
    }

    private fun simulateLockAndUnlock() {
        // Simulate Power Button Press (Lock)
        uiAutomation.executeShellCommand("input keyevent 26")
        Thread.sleep(1000)

        // Simulate Power Button Press (Wake)
        uiAutomation.executeShellCommand("input keyevent 26")
        Thread.sleep(1000)

        // Unlock the device to trigger ACTION_USER_PRESENT
        uiAutomation.executeShellCommand("wm dismiss-keyguard")
        Thread.sleep(2000)
    }

    private fun verifyAppInForeground() {
        assertTrue(
            "$CALENDAR_APP_PACKAGE should be in foreground",
            isAppInForeground()
        )
    }

    private fun isAppInForeground(): Boolean {
        val pfd = uiAutomation.executeShellCommand("dumpsys activity activities | grep mResumedActivity")
        val foregroundApp = ParcelFileDescriptor.AutoCloseInputStream(pfd).use { it.bufferedReader().readText() }
        return foregroundApp.contains(CALENDAR_APP_PACKAGE)
    }

    companion object {
        private const val CALENDAR_APP_NAME = "Calendar"
        private const val CALENDAR_APP_PACKAGE = "com.android.calendar"
    }
}
