package com.galaxy.airviewdictionary.ui.screen.overlay.targethandle

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.PixelFormat
import android.graphics.Point
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.asAndroidPath
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.galaxy.airviewdictionary.R
import com.galaxy.airviewdictionary.core.OverlayService
import com.galaxy.airviewdictionary.data.local.preference.PreferenceRepository
import com.galaxy.airviewdictionary.data.local.screen.ScreenInfo
import com.galaxy.airviewdictionary.data.local.screen.ScreenInfoHolder
import com.galaxy.airviewdictionary.extensions.toPx
import com.galaxy.airviewdictionary.ui.screen.overlay.Event
import com.galaxy.airviewdictionary.ui.screen.overlay.OverlayView
import javax.inject.Singleton


/**
 * 번역 뷰
 */
@Singleton
open class SayHereView : OverlayView() {

    companion object {
        val INSTANCE: SayHereView by lazy(LazyThreadSafetyMode.SYNCHRONIZED) { SayHereView() }
    }

    private lateinit var targetHandleViewModel: TargetHandleViewModel

    override lateinit var layoutParams: WindowManager.LayoutParams

    private val start = mutableStateOf(true)

    override val composable: @Composable () -> Unit = @Composable {
        if (start.value) SayHereL() else SayHereR()
    }

    override val touchListener: (Context) -> View.OnTouchListener? = {
        object : View.OnTouchListener {
            @SuppressLint("ClickableViewAccessibility")
            override fun onTouch(v: View, event: MotionEvent): Boolean {
                targetHandleViewModel.preferenceRepository.update(
                    if (start.value) PreferenceRepository.IS_SAY_HERE_L_SHOWN else PreferenceRepository.IS_SAY_HERE_R_SHOWN,
                    true
                )
                clear()
                return true
            }
        }
    }

    suspend fun cast(applicationContext: Context, start: Boolean, position: Point) {
        val screenInfo: ScreenInfo = ScreenInfoHolder.get()
        layoutParams = WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            if (start) position.x else screenInfo.width,
            position.y + 38.dp.toPx(applicationContext),
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
                    or WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.TOP or Gravity.START
        }
        this.start.value = start
        super.cast(applicationContext)
    }

    override fun onServiceConnected(overlayService: OverlayService) {
        targetHandleViewModel = overlayService.getTargetHandleViewModel()
        super.onServiceConnected(overlayService)
    }

    override fun onOverlayServiceEvent(overlayService: OverlayService, event: Event) {
        when (event) {
            Event.ConfigurationChanged -> {
                clear()
            }

            else -> {}
        }
        super.onOverlayServiceEvent(overlayService, event)
    }
}


@Composable
fun SayHereR() = SayHereBubble(tailOnRight = true)

@Composable
fun SayHereL() = SayHereBubble(tailOnRight = false)

/**
 * 텍스트 길이에 맞춰 폭이 늘어나는 말풍선.
 * 고정 크기 Canvas 대신, 콘텐츠(핸들 아이콘 + 문구) 크기에 맞춰 [drawBehind] 로 말풍선을 그린다.
 * 꼬리는 [tailOnRight] 에 따라 오른쪽/왼쪽 변에 붙는다.
 */
@Composable
private fun SayHereBubble(tailOnRight: Boolean) {
    // 꼬리가 차지하는 가로 폭. 이 폭만큼 꼬리 쪽에 여백을 더 둬서 몸통과 겹치지 않게 한다.
    val tailWidth = 12.dp
    // 몸통 안쪽 여백(문구가 말풍선 테두리에 닿지 않도록).
    val bodyPaddingH = 14.dp
    val bodyPaddingV = 10.dp

    Box(
        modifier = Modifier
            .padding(16.dp) // 그림자와 화면 가장자리 여유
            .wrapContentSize(),
    ) {
        Row(
            modifier = Modifier
                .wrapContentSize()
                .drawBehind { drawSpeechBubble(tailOnRight, tailWidth.toPx()) }
                .padding(
                    start = if (tailOnRight) bodyPaddingH else bodyPaddingH + tailWidth,
                    end = if (tailOnRight) bodyPaddingH + tailWidth else bodyPaddingH,
                    top = bodyPaddingV,
                    bottom = bodyPaddingV,
                ),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Image(
                painter = painterResource(id = R.drawable.ic_drag_handle),
                contentDescription = null,
                modifier = Modifier.size(22.dp),
                colorFilter = ColorFilter.tint(Color(0xFF6a91b2)),
            )

            Spacer(modifier = Modifier.width(6.dp))

            Text(
                text = stringResource(id = R.string.say_here),
                color = Color.Black,
            )
        }
    }
}

/**
 * 현재 draw 영역([size]) 전체를 채우는 말풍선 경로를 그린다.
 * 몸통은 둥근 사각형이고, [tailOnRight] 쪽 변에 세로 중앙을 가리키는 삼각형 꼬리가 붙는다.
 */
private fun DrawScope.drawSpeechBubble(tailOnRight: Boolean, tailWidthPx: Float) {
    val width = size.width
    val height = size.height
    val cornerRadius = 16.dp.toPx()
    val tailHeight = 16.dp.toPx()
    val tailCenterY = height / 2f

    val bubblePath = Path().apply {
        if (tailOnRight) {
            val right = width - tailWidthPx // 몸통 오른쪽 변(꼬리 시작점)

            // Top-left corner
            moveTo(cornerRadius, 0f)
            arcTo(
                rect = Rect(0f, 0f, cornerRadius * 2, cornerRadius * 2),
                startAngleDegrees = 180f,
                sweepAngleDegrees = 90f,
                forceMoveTo = false
            )
            lineTo(right - cornerRadius, 0f)

            // Top-right corner
            arcTo(
                rect = Rect(right - cornerRadius * 2, 0f, right, cornerRadius * 2),
                startAngleDegrees = 270f,
                sweepAngleDegrees = 90f,
                forceMoveTo = false
            )

            // Tail (points right, vertically centered)
            lineTo(right, tailCenterY - tailHeight / 2f)
            lineTo(width, tailCenterY)
            lineTo(right, tailCenterY + tailHeight / 2f)
            lineTo(right, height - cornerRadius)

            // Bottom-right corner
            arcTo(
                rect = Rect(right - cornerRadius * 2, height - cornerRadius * 2, right, height),
                startAngleDegrees = 0f,
                sweepAngleDegrees = 90f,
                forceMoveTo = false
            )
            lineTo(cornerRadius, height)

            // Bottom-left corner
            arcTo(
                rect = Rect(0f, height - cornerRadius * 2, cornerRadius * 2, height),
                startAngleDegrees = 90f,
                sweepAngleDegrees = 90f,
                forceMoveTo = false
            )
            lineTo(0f, cornerRadius)
            close()
        } else {
            val left = tailWidthPx // 몸통 왼쪽 변(꼬리 시작점)

            // Top-left corner
            moveTo(left, 0f)
            arcTo(
                rect = Rect(left, 0f, left + cornerRadius * 2, cornerRadius * 2),
                startAngleDegrees = 180f,
                sweepAngleDegrees = 90f,
                forceMoveTo = false
            )
            lineTo(width - cornerRadius, 0f)

            // Top-right corner
            arcTo(
                rect = Rect(width - cornerRadius * 2, 0f, width, cornerRadius * 2),
                startAngleDegrees = 270f,
                sweepAngleDegrees = 90f,
                forceMoveTo = false
            )
            lineTo(width, height - cornerRadius)

            // Bottom-right corner
            arcTo(
                rect = Rect(width - cornerRadius * 2, height - cornerRadius * 2, width, height),
                startAngleDegrees = 0f,
                sweepAngleDegrees = 90f,
                forceMoveTo = false
            )
            lineTo(left + cornerRadius, height)

            // Bottom-left corner
            arcTo(
                rect = Rect(left, height - cornerRadius * 2, left + cornerRadius * 2, height),
                startAngleDegrees = 90f,
                sweepAngleDegrees = 90f,
                forceMoveTo = false
            )

            // Tail (points left, vertically centered)
            lineTo(left, tailCenterY + tailHeight / 2f)
            lineTo(0f, tailCenterY)
            lineTo(left, tailCenterY - tailHeight / 2f)
            close()
        }
    }

    // Draw shadow
    drawIntoCanvas { canvas ->
        val shadowPaint = android.graphics.Paint().apply {
            color = android.graphics.Color.GRAY
            setShadowLayer(8f, 4f, 4f, android.graphics.Color.GRAY)
        }
        canvas.nativeCanvas.drawPath(bubblePath.asAndroidPath(), shadowPaint)
    }

    // Draw the bubble
    drawPath(path = bubblePath, color = Color.White)
}


















