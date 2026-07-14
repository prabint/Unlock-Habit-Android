package prabin.timsina.unlockhabit

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.ui.NavDisplay
import dagger.hilt.android.AndroidEntryPoint
import prabin.timsina.unlockhabit.permissions.RequestAllPermissions
import prabin.timsina.unlockhabit.ui.screens.app_picker.AppPickerScreen
import prabin.timsina.unlockhabit.ui.screens.home.HomeScreen
import prabin.timsina.unlockhabit.ui.theme.AppTheme

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()

        setContent {
            val navBackStack = rememberNavBackStack(AppRoutes.HomeScreen)

            AppTheme {
                Scaffold(
                    topBar = { AppTopBar(navBackStack) },
                ) { paddingValues ->
                    AppNavDisplay(
                        modifier = Modifier.padding(paddingValues),
                        backStack = navBackStack,
                    )
                }
            }

            RequestAllPermissions()
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun AppTopBar(backStack: NavBackStack<NavKey>) {
    val appRoute = backStack.lastOrNull() as AppRoutes

    CenterAlignedTopAppBar(
        title = {
            Text(
                text = when (appRoute) {
                    AppRoutes.HomeScreen -> stringResource(R.string.app_name)
                    AppRoutes.AppPickerScreen -> stringResource(R.string.select_app)
                }
            )
        },
        navigationIcon = {
            when (appRoute) {
                AppRoutes.HomeScreen -> Unit
                AppRoutes.AppPickerScreen -> {
                    IconButton(onClick = { backStack.removeLastOrNull() }) {
                        Icon(Icons.AutoMirrored.Default.ArrowBack, null)
                    }
                }
            }
        }
    )
}

@Composable
internal fun AppNavDisplay(
    modifier: Modifier = Modifier,
    backStack: NavBackStack<NavKey>,
) {
    NavDisplay(
        modifier = modifier,
        backStack = backStack,
        onBack = { backStack.removeLastOrNull() },
        entryProvider = entryProvider {
            entry<AppRoutes.HomeScreen> {
                HomeScreen(
                    onClickAppPicker = {
                        backStack.add(AppRoutes.AppPickerScreen)
                    }
                )
            }
            entry<AppRoutes.AppPickerScreen> {
                AppPickerScreen(
                    onAppSelectionComplete = {
                        backStack.removeLastOrNull()
                    }
                )
            }
        },
    )
}
