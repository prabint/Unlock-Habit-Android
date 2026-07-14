package prabin.timsina.unlockhabit.ui.theme

import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable

@Composable
fun PreviewWrapper(content: @Composable () -> Unit) {
    AppTheme {
        Surface {
            content()
        }
    }
}
