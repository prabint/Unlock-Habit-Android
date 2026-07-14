package prabin.timsina.unlockhabit.ui.screens.app_picker

import android.graphics.Canvas
import android.graphics.drawable.Drawable
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.graphics.createBitmap
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import prabin.timsina.unlockhabit.ui.screens.dummyDrawable
import prabin.timsina.unlockhabit.ui.theme.PreviewWrapper

@Composable
fun AppPickerScreen(
    viewModel: AppPickerScreenViewModel = hiltViewModel(),
    onAppSelectionComplete: () -> Unit,
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        viewModel.navigationEvent.collect {
            onAppSelectionComplete()
        }
    }

    Content(
        uiState = uiState,
        onAction = { viewModel.onAction(it) },
    )
}

@Composable
private fun Content(
    uiState: AppPickerScreenState,
    onAction: (AppPickerAction) -> Unit,
) {
    if (uiState.isLoading) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center
        ) {
            CircularProgressIndicator()
        }
    }
    LazyColumn {
        items(uiState.allApps) { appInfo ->
            AppIconView(
                appInfo = appInfo,
                onAppSelected = { appInfo ->
                    onAction(AppPickerAction.OnAppSelected(appInfo))
                },
            )
        }
    }
}

@Composable
private fun AppIconView(
    appInfo: AppInfo,
    onAppSelected: (AppInfo) -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onAppSelected(appInfo) }
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        AppIcon(appInfo.icon, modifier = Modifier.size(48.dp))
        Spacer(modifier = Modifier.width(16.dp))
        Text(text = appInfo.name, style = MaterialTheme.typography.bodyLarge)
    }
}

@Composable
fun AppIcon(drawable: Drawable, modifier: Modifier = Modifier) {
    val bitmap = remember(drawable) {
        val bitmap = createBitmap(drawable.intrinsicWidth, drawable.intrinsicHeight)
        val canvas = Canvas(bitmap)
        drawable.setBounds(0, 0, canvas.width, canvas.height)
        drawable.draw(canvas)
        bitmap.asImageBitmap()
    }
    Image(bitmap = bitmap, contentDescription = null, modifier = modifier)
}

@Preview(showBackground = true)
@Composable
private fun AppIconViewPreview() {
    PreviewWrapper {
        AppIconView(
            appInfo = AppInfo(
                name = "Sample App",
                packageName = "com.sample.app",
                icon = dummyDrawable,
            ),
            onAppSelected = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun ContentPreview() {
    val uiState = AppPickerScreenState(
        isLoading = true,
        allApps = listOf(
            AppInfo("App 1", "com.example.app1", dummyDrawable),
            AppInfo("App 2", "com.example.app2", dummyDrawable),
            AppInfo("App 3", "com.example.app3", dummyDrawable)
        )
    )
    PreviewWrapper {
        Content(
            uiState = uiState,
            onAction = {}
        )
    }
}

