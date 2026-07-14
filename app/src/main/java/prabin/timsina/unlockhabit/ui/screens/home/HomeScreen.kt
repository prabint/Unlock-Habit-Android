package prabin.timsina.unlockhabit.ui.screens.home

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import prabin.timsina.unlockhabit.R
import prabin.timsina.unlockhabit.permissions.PermissionsListDialog
import prabin.timsina.unlockhabit.ui.screens.app_picker.AppIcon
import prabin.timsina.unlockhabit.ui.screens.app_picker.AppInfo
import prabin.timsina.unlockhabit.ui.screens.dummyDrawable
import prabin.timsina.unlockhabit.ui.screens.home.HomeScreenAction.OnClickToggleService
import prabin.timsina.unlockhabit.ui.theme.PreviewWrapper

@Composable
fun HomeScreen(
    onClickAppPicker: () -> Unit,
    viewModel: HomeScreenViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    if (uiState.showRationaleDialog) {
        PermissionsListDialog(onDismiss = { viewModel.onAction(HomeScreenAction.OnDismissRationalDialog) })
    }

    Content(
        uiState = uiState,
        onAction = { viewModel.onAction(it) },
        onClickAppPicker = { onClickAppPicker() },
    )
}

@Composable
private fun Content(
    uiState: HomeScreenState,
    onAction: (HomeScreenAction) -> Unit,
    onClickAppPicker: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
    ) {
        if (uiState.preferredApp != null) {
            Text(
                text = stringResource(R.string.status),
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(vertical = 8.dp)
            )

            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = MaterialTheme.shapes.medium,
                color = MaterialTheme.colorScheme.surfaceContainerHigh,
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = when {
                                uiState.isServiceRunning && uiState.isServicePaused -> stringResource(R.string.service_disabled_title)
                                uiState.isServiceRunning -> stringResource(R.string.service_enabled_title)
                                else -> stringResource(R.string.service_disabled_title)
                            },
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = when {
                                uiState.isServiceRunning && uiState.isServicePaused -> stringResource(R.string.service_disabled_desc)
                                uiState.isServiceRunning -> stringResource(R.string.service_enabled_desc)
                                else -> stringResource(R.string.service_disabled_desc)
                            },
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Spacer(modifier = Modifier.width(4.dp))
                    Switch(
                        checked = uiState.isServiceRunning,
                        onCheckedChange = { onAction(OnClickToggleService) }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = stringResource(R.string.app_selection_card_title),
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(vertical = 8.dp)
        )

        OutlinedCard(
            modifier = Modifier.fillMaxWidth(),
            shape = MaterialTheme.shapes.large
        ) {
            Column(
                modifier = Modifier
                    .padding(16.dp)
                    .fillMaxWidth()
            ) {
                uiState.preferredApp?.let { appInfo ->
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        AppIcon(appInfo.icon, modifier = Modifier.size(56.dp))
                        Spacer(modifier = Modifier.width(16.dp))
                        Text(text = appInfo.name, style = MaterialTheme.typography.titleMedium)
                    }
                } ?: Text(
                    text = "Pick an app to automatically open every time you unlock your phone.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = onClickAppPicker,
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                ) {
                    Text(
                        when (uiState.preferredApp) {
                            null -> stringResource(R.string.select_application)
                            else -> stringResource(R.string.change_application)
                        }
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun PreviewContentNoAppSelected() {
    PreviewWrapper {
        Content(
            uiState = HomeScreenState(),
            onAction = {},
            onClickAppPicker = {},
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun PreviewContentAppSelected() {
    PreviewWrapper {
        Content(
            uiState = HomeScreenState(
                isServiceRunning = true,
                preferredApp = AppInfo(
                    name = "Test App",
                    packageName = "com.test.app",
                    icon = dummyDrawable
                ),
            ),
            onAction = {},
            onClickAppPicker = {},
        )
    }
}