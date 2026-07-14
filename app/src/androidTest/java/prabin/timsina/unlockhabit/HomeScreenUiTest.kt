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
    fun powerButtonPressLaunchesCalendar() {
        // Kill calendar app first to ensure it's launched fresh
        uiAutomation.executeShellCommand("am force-stop com.android.calendar")
        Thread.sleep(1000)

        // Verify it's not in the foreground
        val pfdInitial = uiAutomation.executeShellCommand("dumpsys activity activities | grep mResumedActivity")
        val foregroundAppInitial =
            ParcelFileDescriptor.AutoCloseInputStream(pfdInitial).use { it.bufferedReader().readText() }
        assertTrue(
            "Calendar app should not be in foreground at start, but found: $foregroundAppInitial",
            !foregroundAppInitial.contains("com.android.calendar")
        )

        val appName = "Calendar"
        val appContext = InstrumentationRegistry.getInstrumentation().targetContext
        val selectAppText = appContext.getString(R.string.select_application)
        val changeAppText = appContext.getString(R.string.change_application)
        val serviceOnText = appContext.getString(R.string.service_enabled_title)

        // 1. Wait for "Select Application" or "Change Application"
        composeTestRule.waitUntil(timeoutMillis = 5000) {
            composeTestRule.onAllNodesWithText(selectAppText).fetchSemanticsNodes().isNotEmpty() ||
                composeTestRule.onAllNodesWithText(changeAppText).fetchSemanticsNodes().isNotEmpty()
        }

        // 2. Click to select app
        if (composeTestRule.onAllNodesWithText(selectAppText).fetchSemanticsNodes().isNotEmpty()) {
            composeTestRule.onNodeWithText(selectAppText).performClick()
        } else {
            composeTestRule.onNodeWithText(changeAppText).performClick()
        }

        // 3. Wait for app list and pick "Calendar"
        composeTestRule.waitUntil(timeoutMillis = 10000) {
            composeTestRule.onAllNodesWithText(appName).fetchSemanticsNodes().isNotEmpty()
        }
        composeTestRule.onNodeWithText(appName).performClick()

        // 4. Verify on Home and turn service ON if needed
        composeTestRule.onNodeWithText(appName).assertExists()
        if (composeTestRule.onAllNodesWithText(serviceOnText).fetchSemanticsNodes().isEmpty()) {
            composeTestRule.onNode(isToggleable()).performClick()
        }
        composeTestRule.onNodeWithText(serviceOnText).assertExists()

        // 5. Simulate Power Button Press (Lock)
        uiAutomation.executeShellCommand("input keyevent 26")
        Thread.sleep(1000)

        // 6. Simulate Power Button Press (Wake)
        uiAutomation.executeShellCommand("input keyevent 26")
        Thread.sleep(1000)

        // 7. Unlock the device to trigger ACTION_USER_PRESENT
        uiAutomation.executeShellCommand("wm dismiss-keyguard")
        Thread.sleep(2000)

        // 8. Verify the Calendar app is launched by checking if the package is in the foreground
        val pfd = uiAutomation.executeShellCommand("dumpsys activity activities | grep mResumedActivity")
        val foregroundApp = ParcelFileDescriptor.AutoCloseInputStream(pfd).use { it.bufferedReader().readText() }
        assertTrue(
            "Calendar app should be in foreground, but found: $foregroundApp",
            foregroundApp.contains("com.android.calendar")
        )
    }
}
