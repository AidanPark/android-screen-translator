package com.galaxy.airviewdictionary.ui.screen.overlay.targethandle

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.PixelFormat
import android.graphics.Point
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.StartOffset
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.galaxy.airviewdictionary.R
import com.galaxy.airviewdictionary.core.OverlayService
import com.galaxy.airviewdictionary.data.local.preference.PreferenceRepository
import com.galaxy.airviewdictionary.ui.screen.overlay.Event
import com.galaxy.airviewdictionary.ui.screen.overlay.OverlayView
import javax.inject.Singleton


/**
 * 설정 화면이 처음 닫힌 뒤 딱 한 번, 핸들을 두 번 탭하면 설정을 다시 열 수 있음을 알려주는 코치마크.
 * - 화면 전체를 살짝 어둡게 하고, 핸들 위치에 더블탭 펄스를 그려 시선을 유도한다.
 * - 아무 곳이나 탭하면 닫히고, 다시 뜨지 않도록 [PreferenceRepository.IS_SETTINGS_REOPEN_HINT_SHOWN] 를 저장한다.
 */
@Singleton
open class SettingsReopenHintView : OverlayView() {

    companion object {
        val INSTANCE: SettingsReopenHintView by lazy(LazyThreadSafetyMode.SYNCHRONIZED) { SettingsReopenHintView() }
    }

    private lateinit var targetHandleViewModel: TargetHandleViewModel

    override lateinit var layoutParams: WindowManager.LayoutParams

    // 핸들 창의 좌상단(=SayHereView 와 동일한 좌표계). 여기에 핸들 반폭을 더해 중심을 구한다.
    private val handleTopLeft = mutableStateOf(Point(0, 0))

    override val composable: @Composable () -> Unit = @Composable {
        val density = LocalDensity.current
        val configuration = LocalConfiguration.current
        val context = LocalContext.current
        val screenHeightPx = with(density) { configuration.screenHeightDp.dp.toPx() }

        // 펄스를 현재 핸들의 drag_handle 그립 중심에 맞춘다.
        // (핸들 창 = target_handle_width x target_handle_height, handleTopLeft = 그 좌상단)
        val handleHalfWidthPx = with(density) { (dimensionResource(id = R.dimen.target_handle_width) / 2f).toPx() }
        val handleHalfHeightPx = with(density) { (dimensionResource(id = R.dimen.target_handle_height) / 2f).toPx() }
        // 핸들 창 세로 구성: 포인터(target_pointer_dimen) + 간격(pointer_thumb_space) + 썸박스(target_handle_width, 그립은 중앙).
        // → drag_handle 그립의 세로 중심은 창 상단에서 77dp.
        val gripOffsetPx = with(density) {
            (dimensionResource(id = R.dimen.target_pointer_dimen)
                    + dimensionResource(id = R.dimen.target_handle_pointer_thumb_space)
                    + dimensionResource(id = R.dimen.target_handle_width) / 2f).toPx()
        }
        // 코치마크 창은 MATCH_PARENT 라 상태바만큼 아래로 프레임된다(frame top = status bar).
        // 핸들 창은 물리 좌표에 렌더되므로, 이 창 좌표계로 변환하려면 상태바 높이를 빼준다. (X 는 x=0 부터라 보정 불필요)
        val statusBarTopPx = remember(context) {
            val id = context.resources.getIdentifier("status_bar_height", "dimen", "android")
            if (id > 0) context.resources.getDimensionPixelSize(id).toFloat() else 0f
        }
        val centerX = handleTopLeft.value.x + handleHalfWidthPx
        val centerY = handleTopLeft.value.y + gripOffsetPx - statusBarTopPx

        Box(modifier = Modifier.fillMaxSize()) {
            DoubleTapPulse(centerX = centerX, centerY = centerY)

            // 안내 카드: 가로 중앙, 핸들의 위/아래로 배치(핸들이 화면 상단이면 아래, 하단이면 위).
            val gapDp = 28.dp
            val cardEstHeightDp = 92.dp
            val below = centerY < screenHeightPx * 0.55f
            val cardYDp = if (below) {
                with(density) { (centerY + handleHalfHeightPx).toDp() } + gapDp
            } else {
                (with(density) { (centerY - handleHalfHeightPx).toDp() } - gapDp - cardEstHeightDp)
            }.coerceIn(24.dp, with(density) { screenHeightPx.toDp() } - cardEstHeightDp - 24.dp)

            Row(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .offset { IntOffset(0, with(density) { cardYDp.roundToPx() }) }
                    .widthIn(max = 320.dp)
                    .wrapContentSize()
                    .shadow(8.dp, shape = RoundedCornerShape(18.dp))
                    .background(color = Color.White, shape = RoundedCornerShape(18.dp))
                    .padding(horizontal = 20.dp, vertical = 16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Image(
                    painter = painterResource(id = R.drawable.ic_drag_handle),
                    contentDescription = null,
                    modifier = Modifier.size(28.dp),
                    colorFilter = ColorFilter.tint(Color(0xFF6a91b2))
                )
                Spacer(modifier = Modifier.width(14.dp))
                Text(
                    text = stringResource(id = R.string.hint_settings_reopen),
                    color = Color(0xFF222222),
                    textAlign = TextAlign.Start,
                    fontSize = 15.sp
                )
            }
        }
    }

    override val touchListener: (Context) -> View.OnTouchListener? = {
        object : View.OnTouchListener {
            @SuppressLint("ClickableViewAccessibility")
            override fun onTouch(v: View, event: MotionEvent): Boolean {
                if (event.action == MotionEvent.ACTION_DOWN) {
                    targetHandleViewModel.preferenceRepository.update(
                        PreferenceRepository.IS_SETTINGS_REOPEN_HINT_SHOWN,
                        true
                    )
                    clear()
                }
                return true
            }
        }
    }

    suspend fun cast(applicationContext: Context, handleTopLeft: Point) {
        this.handleTopLeft.value = handleTopLeft
        layoutParams = WindowManager.LayoutParams(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
                    or WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN
                    or WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS
                    or WindowManager.LayoutParams.FLAG_DIM_BEHIND,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.TOP or Gravity.START
            dimAmount = 0.55f
        }
        super.cast(applicationContext)
    }

    override fun onServiceConnected(overlayService: OverlayService) {
        targetHandleViewModel = overlayService.getTargetHandleViewModel()
        super.onServiceConnected(overlayService)
    }

    override fun onOverlayServiceEvent(overlayService: OverlayService, event: Event) {
        when (event) {
            Event.ConfigurationChanged -> clear()
            else -> {}
        }
        super.onOverlayServiceEvent(overlayService, event)
    }
}

/**
 * 핸들 위치에 두 개의 원이 시차를 두고 퍼졌다 사라지며 "두 번 탭" 을 암시하는 펄스.
 */
@Composable
private fun DoubleTapPulse(centerX: Float, centerY: Float) {
    val accent = Color(0xFF6a91b2)
    val transition = rememberInfiniteTransition(label = "double tap pulse")
    val p1 by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1400, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "p1"
    )
    val p2 by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1400, easing = LinearEasing),
            repeatMode = RepeatMode.Restart,
            initialStartOffset = StartOffset(280)
        ),
        label = "p2"
    )

    Canvas(modifier = Modifier.fillMaxSize()) {
        val center = Offset(centerX, centerY)
        val minR = 18.dp.toPx()
        val maxR = 54.dp.toPx()
        val stroke = Stroke(width = 3.dp.toPx())
        listOf(p1, p2).forEach { p ->
            val radius = minR + (maxR - minR) * p
            val alpha = (1f - p).coerceIn(0f, 1f) * 0.7f
            drawCircle(color = accent.copy(alpha = alpha), radius = radius, center = center, style = stroke)
        }
        // 중심 점
        drawCircle(color = accent.copy(alpha = 0.9f), radius = 7.dp.toPx(), center = center)
    }
}
