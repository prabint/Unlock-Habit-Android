package prabin.timsina.unlockhabit.ui.screens.overlay.slider

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.AndroidUiModes.UI_MODE_NIGHT_YES
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch
import prabin.timsina.unlockhabit.R
import prabin.timsina.unlockhabit.repository.DefaultUserPreferencesRepository.Companion.MAX_HEARTS
import prabin.timsina.unlockhabit.repository.models.TodayYesterdayCounts
import prabin.timsina.unlockhabit.ui.theme.Green
import prabin.timsina.unlockhabit.ui.theme.PreviewWrapper
import prabin.timsina.unlockhabit.ui.theme.RedHeart
import kotlin.math.roundToInt

@Composable
fun SliderOverlayScreen(
    preferredAppName: String,
    heartsLeft: Int,
    todayYesterdayCounts: TodayYesterdayCounts,
    onAction: (TextOverlayAction) -> Unit,
) {
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(start = 24.dp, end = 24.dp, bottom = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Spacer(modifier = Modifier.weight(1f))
                IconButton(
                    onClick = { onAction(TextOverlayAction.OpenSettings) },
                ) {
                    Image(
                        painter = painterResource(R.drawable.ic_launcher_foreground),
                        contentDescription = "App icon",
                    )
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            CenterTextContent(todayYesterdayCounts = todayYesterdayCounts)

            Spacer(modifier = Modifier.weight(1f))

            Button(
                onClick = { onAction(TextOverlayAction.OpenApp) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(16.dp)
            ) {
                Text(
                    stringResource(R.string.open_app_name, preferredAppName),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            SliderContainer(
                livesLeft = heartsLeft,
                onUnlocked = { onAction(TextOverlayAction.Skip) }
            )
        }
    }
}

@Composable
private fun CenterTextContent(todayYesterdayCounts: TodayYesterdayCounts) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = stringResource(R.string.total_unlocks),
            style = MaterialTheme.typography.headlineLarge,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
            lineHeight = 26.sp,
        )

        Spacer(modifier = Modifier.height(16.dp))

        HorizontalDivider(
            modifier = Modifier.width(40.dp),
            thickness = 3.dp,
        )

        Spacer(modifier = Modifier.height(16.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 20.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Text(
                        text = stringResource(R.string.yesterday),
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = "${todayYesterdayCounts.yesterday}",
                        style = MaterialTheme.typography.headlineLarge,
                        fontWeight = FontWeight.Bold,
                    )
                }

                VerticalDivider(
                    modifier = Modifier.height(56.dp),
                    color = MaterialTheme.colorScheme.outlineVariant,
                )

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Text(
                        text = stringResource(R.string.today),
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = "${todayYesterdayCounts.today}",
                        style = MaterialTheme.typography.headlineLarge,
                        fontWeight = FontWeight.Bold,
                        color = if (todayYesterdayCounts.today > todayYesterdayCounts.yesterday) {
                            RedHeart
                        } else {
                            Green
                        },
                    )
                }
            }
        }
    }
}

@Composable
private fun Slider(onUnlocked: () -> Unit) {
    val offsetX = remember { Animatable(0f) }
    val coroutineScope = rememberCoroutineScope()
    val haptic = LocalHapticFeedback.current

    var isUnlocked by remember { mutableStateOf(false) }

    val boxSize = 56.dp

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxWidth()
            .height(64.dp)
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.3f))
            .border(1.dp, MaterialTheme.colorScheme.outlineVariant, CircleShape),
        contentAlignment = Alignment.Center,
    ) {
        val density = LocalDensity.current
        val maxPx = with(density) { (maxWidth - boxSize - 8.dp).toPx() }
        val progress = when {
            maxPx > 0f -> (offsetX.value / maxPx).coerceIn(0f, 1f)
            else -> 0f
        }

        val animatedProgress by animateFloatAsState(
            targetValue = progress,
            animationSpec = tween(100),
            label = "trackProgress",
        )

        val trackColor = lerp(
            MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.1f),
            MaterialTheme.colorScheme.primary.copy(alpha = 0.2f),
            animatedProgress,
        )

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(trackColor),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                modifier = Modifier
                    .fillMaxWidth()
                    .graphicsLayer(alpha = 1f - animatedProgress),
                text = stringResource(R.string.use_one_heart),
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontWeight = FontWeight.Medium
            )
            Box(
                modifier = Modifier
                    .offset {
                        IntOffset(
                            x = offsetX.value.roundToInt() + with(density) { 4.dp.toPx() }.roundToInt(),
                            y = 0
                        )
                    }
                    .size(boxSize)
                    .clip(CircleShape)
                    .align(Alignment.CenterStart)
                    .background(
                        brush = Brush.linearGradient(
                            colors = listOf(
                                MaterialTheme.colorScheme.primary,
                                MaterialTheme.colorScheme.primaryContainer
                            )
                        )
                    )
                    .draggable(
                        enabled = !isUnlocked,
                        orientation = Orientation.Horizontal,
                        state = rememberDraggableState { delta ->
                            val newOffset = (offsetX.value + delta).coerceIn(0f, maxPx)
                            haptic.performHapticFeedback(HapticFeedbackType.Reject)
                            coroutineScope.launch { offsetX.snapTo(newOffset) }

                            if (!isUnlocked && newOffset >= maxPx) {
                                isUnlocked = true
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                onUnlocked()
                            }
                        },
                        onDragStopped = {
                            if (!isUnlocked) {
                                coroutineScope.launch { offsetX.animateTo(targetValue = 0f, animationSpec = spring()) }
                                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            }
                        },
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Default.KeyboardArrowRight,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier.size(32.dp)
                )
            }
        }
    }
}

@Composable
private fun LivesIndicator(
    livesLeft: Int,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(
            12.dp,
            Alignment.CenterHorizontally,
        ),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        repeat(MAX_HEARTS) { index ->
            val isFilled = index < livesLeft
            val heartColor = if (isFilled) RedHeart else MaterialTheme.colorScheme.outlineVariant
            val scale = if (isFilled) 1f else 0.8f

            Icon(
                imageVector = Icons.Default.Favorite,
                contentDescription = null,
                modifier = Modifier
                    .size(24.dp)
                    .scale(scale),
                tint = heartColor,
            )
        }
    }
}

@Composable
private fun SliderContainer(
    livesLeft: Int,
    onUnlocked: () -> Unit,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainer,
        ),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            LivesIndicator(livesLeft = livesLeft)

            Spacer(modifier = Modifier.height(16.dp))

            if (livesLeft > 0) {
                Text(
                    text = stringResource(R.string.slide_to_continue_to_your_phone),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                )
                Spacer(modifier = Modifier.height(8.dp))

                Slider(onUnlocked = onUnlocked)
            } else {
                Text(
                    text = stringResource(R.string.you_have_used_your_hearts_for_today),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    textAlign = TextAlign.Center,
                )

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = stringResource(R.string.your_hearts_will_be_refreshed_tomorrow),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun PreviewContent() {
    PreviewWrapper {
        SliderOverlayScreen(
            preferredAppName = "Kindle",
            heartsLeft = 3,
            todayYesterdayCounts = TodayYesterdayCounts(10, 5),
            onAction = {},
        )
    }
}

@Preview(showBackground = true, uiMode = UI_MODE_NIGHT_YES)
@Composable
private fun PreviewContentNightMode() {
    PreviewWrapper {
        SliderOverlayScreen(
            preferredAppName = "Kindle",
            heartsLeft = 3,
            todayYesterdayCounts = TodayYesterdayCounts(10, 5),
            onAction = {},
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun PreviewSliderContainer() {
    PreviewWrapper {
        SliderContainer(
            livesLeft = 0,
            onUnlocked = {},
        )
    }
}

@Preview(showBackground = true, uiMode = UI_MODE_NIGHT_YES)
@Composable
private fun PreviewSliderContainerNightMode() {
    PreviewWrapper {
        SliderContainer(
            livesLeft = 0,
            onUnlocked = {},
        )
    }
}
