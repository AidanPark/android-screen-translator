package com.galaxy.airviewdictionary.ui.screen.overlay.fixedarea


import android.content.Context
import android.graphics.PixelFormat
import android.graphics.Rect
import android.view.Gravity
import android.view.WindowManager
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.galaxy.airviewdictionary.ui.screen.overlay.OverlayView
import kotlinx.coroutines.delay
import javax.inject.Singleton


/**
 * 고정 선택 영역 번역 뷰
 */
@Singleton
open class FixedAreaTranslationView : OverlayView() {

    companion object {
        val INSTANCE: FixedAreaTranslationView by lazy(LazyThreadSafetyMode.SYNCHRONIZED) { FixedAreaTranslationView() }

        private const val MINIMUM_DISPLAY_DURATION_MS = 700L
    }

    override lateinit var layoutParams: WindowManager.LayoutParams

    override val composable: @Composable () -> Unit = @Composable {
        val rawTranslation by FixedAreaView.translationFlow.collectAsStateWithLifecycle()
        val visibleTranslation = remember { mutableStateOf("") }
        val lastShownTime = remember { mutableLongStateOf(0L) }

        LaunchedEffect(rawTranslation) {
            val now = System.currentTimeMillis()
            val elapsed = now - lastShownTime.value

            if (visibleTranslation.value.isBlank() && rawTranslation.isNotBlank()) {
                // "" → 텍스트: 즉시 표시
                visibleTranslation.value = rawTranslation
                lastShownTime.value = now
            } else {
                // 나머지는 MINIMUM_DISPLAY_DURATION_MS 유지
                if (elapsed < MINIMUM_DISPLAY_DURATION_MS) {
                    delay(MINIMUM_DISPLAY_DURATION_MS - elapsed)
                }
                visibleTranslation.value = rawTranslation
                lastShownTime.value = System.currentTimeMillis()
            }
        }

        Box(
            modifier = Modifier
                .fillMaxSize(),
//                .background(Color.Black.copy(alpha = 0.2f))
            contentAlignment = Alignment.BottomCenter,
        ) {
            val text = visibleTranslation.value
            val fontSize = 17.sp
            val outlineColor = Color(0x77000000)
            val mainColor = Color(0xFFfefefe)
            val offset = 1.2.dp

            Text(
                text = text,
                color = outlineColor,
                fontSize = fontSize,
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.bodyLarge.copy(
                    shadow = Shadow(
                        color = Color.Black,
                        offset = Offset(0f, 0f), // 이동은 offset()으로 처리하므로 여기선 0
                        blurRadius = 6f // 블러 효과
                    )
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .offset(x = offset, y = offset) // 3dp 오른쪽/아래로 이동
            )

            Text(
                text = text,
                color = mainColor,
                fontSize = fontSize,
                style = MaterialTheme.typography.bodyLarge,
                textAlign = TextAlign.Center,
            )
        }

    }

    open suspend fun cast(
        applicationContext: Context,
        fixedArea: Rect
    ) {
        val width: Int = (fixedArea.width() * .9).toInt()
        val height: Int = (fixedArea.height() * .8).toInt()
        layoutParams = WindowManager.LayoutParams(
            WindowManager.LayoutParams.MATCH_PARENT,
            height,
            0,
            fixedArea.top - height,
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
            WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN
                    or WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.TOP or Gravity.START
        }
        super.cast(applicationContext)
    }
}



















