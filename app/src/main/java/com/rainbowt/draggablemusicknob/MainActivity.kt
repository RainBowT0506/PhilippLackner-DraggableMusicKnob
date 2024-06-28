package com.rainbowt.draggablemusicknob

import android.os.Bundle
import android.view.MotionEvent
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
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
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInteropFilter
import androidx.compose.ui.layout.boundsInWindow
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import kotlin.math.PI
import kotlin.math.atan2
import kotlin.math.roundToInt

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
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
                        .border(1.dp, Color.Green, RoundedCornerShape(10.dp))
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
    }
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
                drawRoundRect(
                    // 設置條形的顏色，活躍的條形為綠色，否則為深灰色
                    color = if (i in 0..activeBars) Color.Green else Color.DarkGray,
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

    Image(
        painter = painterResource(id = R.drawable.ic_music_knob),
        contentDescription = "Music Knob",
        modifier = modifier
            .fillMaxSize()
            // 獲取圖片在窗口中的位置
            .onGloballyPositioned {
                // 獲取圖片的邊界
                val windowBounds = it.boundsInWindow()
                // 計算旋鈕中心的坐標
                knobCenterX = windowBounds.size.width / 2f
                knobCenterY = windowBounds.size.height / 2f
            }
            // 設置指針交互過濾器來處理觸摸事件
            .pointerInteropFilter { event ->
                // 記錄觸摸點的坐標
                touchX = event.x
                touchY = event.y

                // 計算觸摸點與圓心之間的角度
                val touchAngle =
                    calculateTouchAngle(knobCenterX, touchX, knobCenterY, touchY)

                when (event.action) {
                    MotionEvent.ACTION_DOWN,
                    MotionEvent.ACTION_MOVE -> {
                        if (isAngleNotWithinLimits(touchAngle, limitingAngle)) {
                            // 設置旋轉角度
                            currentRotation = calculateCorrectAngle(touchAngle, limitingAngle)
                            // 計算旋轉百分比
                            val rotationPercentage =
                                calculateRotationPercentage(currentRotation, limitingAngle)
                            onValueChange(rotationPercentage)
                            true
                        } else false
                    }

                    else -> false
                }
            }
            // 根據旋轉角度旋轉圖片
            .rotate(currentRotation)
    )
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

