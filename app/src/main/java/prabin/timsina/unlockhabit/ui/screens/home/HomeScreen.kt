package prabin.timsina.unlockhabit.ui.screens.home

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.clickable
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
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.RadioButton
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
        onClickLaunchDirectly = { viewModel.onAction(HomeScreenAction.OnClickLaunchDirectly) },
        onClickShowOverlay = { viewModel.onAction(HomeScreenAction.OnClickShowOverlay) }
    )
}

@Composable
private fun Content(
    uiState: HomeScreenState,
    onAction: (HomeScreenAction) -> Unit,
    onClickAppPicker: () -> Unit,
    onClickLaunchDirectly: () -> Unit,
    onClickShowOverlay: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
    ) {
        Spacer(modifier = Modifier.height(8.dp))

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
                    text = stringResource(R.string.pick_app_instruction),
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

        if (uiState.preferredApp != null) {
            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = stringResource(R.string.status),
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(vertical = 8.dp)
            )

            OutlinedCard(
                modifier = Modifier.fillMaxWidth(),
                shape = MaterialTheme.shapes.large
            ) {
                ListItem(
                    headlineContent = {
                        Text(
                            text = stringResource(R.string.home_service_toggle_title),
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.Bold
                        )
                    },
                    supportingContent = {
                        Text(
                            text = stringResource(R.string.home_service_toggle_desc),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    },
                    trailingContent = {
                        Switch(
                            checked = uiState.isServiceRunning,
                            onCheckedChange = { onAction(OnClickToggleService(it)) }
                        )
                    }
                )

                AnimatedVisibility(visible = uiState.isServiceRunning) {
                    Column {
                        HorizontalDivider(
                            modifier = Modifier.padding(horizontal = 16.dp),
                            color = MaterialTheme.colorScheme.outlineVariant
                        )

                        Text(
                            text = stringResource(R.string.display_option),
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(start = 16.dp, top = 16.dp, bottom = 8.dp)
                        )

                        ListItem(
                            headlineContent = {
                                Text(stringResource(R.string.launch_directly_title))
                            },
                            supportingContent = {
                                Text(stringResource(R.string.launch_directly_desc))
                            },
                            trailingContent = {
                                RadioButton(
                                    selected = uiState.shouldLaunchDirectly,
                                    onClick = null
                                )
                            },
                            modifier = Modifier.clickable { onClickLaunchDirectly() }
                        )

                        ListItem(
                            headlineContent = {
                                Text(stringResource(R.string.show_reminder_first))
                            },
                            supportingContent = {
                                Text(stringResource(R.string.overlay_row_desc))
                            },
                            trailingContent = {
                                RadioButton(
                                    selected = !uiState.shouldLaunchDirectly,
                                    onClick = null
                                )
                            },
                            modifier = Modifier.clickable { onClickShowOverlay() }
                        )
                    }
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
            onClickLaunchDirectly = {},
            onClickShowOverlay = {},
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
            onClickLaunchDirectly = {},
            onClickShowOverlay = {},
        )
    }
}