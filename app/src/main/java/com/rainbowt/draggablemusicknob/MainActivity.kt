package com.rainbowt.draggablemusicknob

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.MotionEvent
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.TileMode
import androidx.compose.ui.input.pointer.pointerInteropFilter
import androidx.compose.ui.layout.boundsInWindow
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlin.math.PI
import kotlin.math.atan2
import kotlin.math.roundToInt

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            DraggableMusicKnob()
        }
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewDraggableMusicKnob() {
    DraggableMusicKnob()
}

@Composable
private fun DraggableMusicKnob() {
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        Row(
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .rainbowBorder(1.dp, 10.dp)
                .padding(30.dp)
        ) {
            var volume by remember {
                mutableStateOf(0f)
            }

            MusicKnob(modifier = Modifier.size(100.dp)) {
                volume = it
            }

            Spacer(modifier = Modifier.width(20.dp))

            val barCount = 20
            VolumeBar(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(30.dp),
                activeBars = (barCount * volume).roundToInt(),
                barCount = barCount
            )
        }
    }
}

// Custom Modifier for Rainbow Border
@SuppressLint("ModifierFactoryUnreferencedReceiver")
fun Modifier.rainbowBorder(width: Dp, cornerRadius: Dp): Modifier = composed {
    val infiniteTransition = rememberInfiniteTransition(label = "RainbowBorderInfiniteTransition")
    val colorShift by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = 3000,
                easing = LinearEasing
            ),
            repeatMode = RepeatMode.Restart
        ),
        label = "ColorShiftAnimation"
    )

    val rainbowColors = listOf(
        Color.Red,
        Color.Yellow,
        Color.Green,
        Color.Cyan,
        Color.Blue,
        Color.Magenta,
        Color.Red
    )

    val brush = Brush.linearGradient(
        colors = rainbowColors,
        start = Offset(colorShift * 1000f, 0f),
        end = Offset((colorShift + 1f) * 1000f, 0f),
        tileMode = TileMode.Repeated
    )

    border(
        width = width,
        brush = brush,
        shape = RoundedCornerShape(cornerRadius)
    )
}

/**
 * 一個可組合函數，用於創建具有指定數量活躍條形的音量條。
 *
 * @param modifier 用於應用於音量條的 [Modifier]。默認為空的 [Modifier]。
 * @param activeBars 活躍條形（填充綠色）的數量。默認為 0。
 * @param barCount 音量條中的總條形數量。默認為 10。
 */
@Composable
fun VolumeBar(
    modifier: Modifier = Modifier,
    activeBars: Int = 0,
    barCount: Int = 10
) {
    BoxWithConstraints(
        contentAlignment = Alignment.Center,
        modifier = modifier,
    ) {
        // 計算每個條形的寬度
        val barWidth = remember {
            constraints.maxWidth / (2f * barCount)
        }

        Canvas(modifier = modifier) {
            for (i in 0 until barCount) {

                val hue = (i * 360f / barCount) % 360
                val color = Color.hsv(hue, 1f, if (i <= activeBars) 1f else 0.3f)

                drawRoundRect(
                    // 設置條形的顏色，活躍的條形為綠色，否則為深灰色
                    color = color,
                    // 設置條形的頂部左側位置
                    topLeft = Offset(i * barWidth * 2f + barWidth / 2f, 0f),
                    // 設置條形的大小
                    size = Size(barWidth, constraints.maxHeight.toFloat()),
                    // 設置條形的圓角半徑
                    cornerRadius = CornerRadius(0f)
                )
            }
        }
    }
}

/**
 * 一個可組合的函數，用於創建一個可自定義的音樂旋鈕控件，該旋鈕可以旋轉以改變其值。
 * 旋轉角度受指定的限制角度限制。
 *
 * @param modifier 用於應用於音樂旋鈕的 [Modifier]。默認為空的 [Modifier]。
 * @param limitingAngle 限制旋轉的角度（以度為單位）。旋轉將限制在 -limitingAngle 和 +limitingAngle 之間。
 *                      默認為 25 度。
 * @param onValueChange 每當旋鈕旋轉改變時調用的回調函數。它提供當前旋轉值，作為總旋轉的百分比（0.0 到 1.0）。
 *
 **/
@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun MusicKnob(
    modifier: Modifier = Modifier,
    limitingAngle: Float = 25f,
    onValueChange: (Float) -> Unit
) {
    // 旋轉角度，初始設為限制角度
    var currentRotation by remember {
        mutableStateOf(limitingAngle)
    }

    // 觸摸點的X和Y坐標
    var touchX by remember {
        mutableStateOf(0f)
    }
    var touchY by remember {
        mutableStateOf(0f)
    }

    // 旋鈕中心的X和Y坐標
    var knobCenterX by remember {
        mutableStateOf(0f)
    }
    var knobCenterY by remember {
        mutableStateOf(0f)
    }

    val rotationPercentage = calculateRotationPercentage(currentRotation, limitingAngle)
    val hue = (rotationPercentage * 360) % 360
    val knobColor = Color.hsv(hue, 1f, 1f)
    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .size(150.dp) // Adjust the size as needed
            .background(knobColor, shape = RoundedCornerShape(50.dp)) // Circular background
            .onGloballyPositioned {
                val windowBounds = it.boundsInWindow()
                knobCenterX = windowBounds.size.width / 2f
                knobCenterY = windowBounds.size.height / 2f
            }
            .pointerInteropFilter { event ->
                touchX = event.x
                touchY = event.y

                val touchAngle = calculateTouchAngle(knobCenterX, touchX, knobCenterY, touchY)

                when (event.action) {
                    MotionEvent.ACTION_DOWN,
                    MotionEvent.ACTION_MOVE -> {
                        if (isAngleNotWithinLimits(touchAngle, limitingAngle)) {
                            currentRotation = calculateCorrectAngle(touchAngle, limitingAngle)
                            val rotationPercentage =
                                calculateRotationPercentage(currentRotation, limitingAngle)
                            onValueChange(rotationPercentage)
                            true
                        } else false
                    }

                    else -> false
                }
            }
    ) {
        Image(
            painter = painterResource(id = R.drawable.ic_music_knob),
            contentDescription = "Music Knob",
            modifier = Modifier
                .size(100.dp)
                .rotate(currentRotation)
        )
    }
}

/**
 * 計算觸摸點與旋鈕中心之間的角度
 */
private fun calculateTouchAngle(
    knobCenterX: Float,
    touchX: Float,
    knobCenterY: Float,
    touchY: Float
) = -atan2(knobCenterX - touchX, knobCenterY - touchY) * (180f / PI).toFloat()

/**
 * 計算旋轉百分比
 */
private fun calculateRotationPercentage(currentRotation: Float, limitingAngle: Float) =
    (currentRotation - limitingAngle) / (360f - 2 * limitingAngle)

/**
 * 固定角度，如果角度在 -180 度到 -限制角度之間，則加上 360 度
 */
private fun calculateCorrectAngle(touchAngle: Float, limitingAngle: Float) =
    if (touchAngle in -180f..-limitingAngle) {
        360f + touchAngle
    } else {
        touchAngle
    }

/**
 * 如果角度不在限制角度範圍內
 */
private fun isAngleNotWithinLimits(touchAngle: Float, limitingAngle: Float) =
    touchAngle !in -limitingAngle..limitingAngle

