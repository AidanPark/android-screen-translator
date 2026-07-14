package com.galaxy.airviewdictionary.ui.screen.overlay.settings


import android.content.Context
import android.graphics.PixelFormat
import android.graphics.Point
import android.view.Gravity
import android.view.WindowManager
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.galaxy.airviewdictionary.R
import com.galaxy.airviewdictionary.ui.screen.overlay.OverlayView
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import javax.inject.Singleton

/**
 * 메뉴 TTS speech rate 뷰
 */
@Singleton
class SettingsTTSSpeechRateView private constructor() : OverlayView() {

    companion object {
        val INSTANCE: SettingsTTSSpeechRateView by lazy(LazyThreadSafetyMode.SYNCHRONIZED) { SettingsTTSSpeechRateView() }
    }

    override lateinit var layoutParams: WindowManager.LayoutParams

    private val floatFlow = mutableStateOf<MutableStateFlow<Float>?>(null)

    override val composable: @Composable () -> Unit = @Composable {
        if (isAttachedToWindow() && floatFlow.value != null) {
            val ttsSpeechRate by floatFlow.value!!.collectAsStateWithLifecycle()
            /*
                Speech rate. 1.0 is the normal speech rate,
                lower values slow down the speech (0.5 is half the normal speech rate),
                greater values accelerate it (2.0 is twice the normal speech rate).
             */
            val imageResource = remember { mutableIntStateOf(R.drawable.tts_rate_0) }
            LaunchedEffect(key1 = true) {
                while (true) {
                    delay((((2.2f - ttsSpeechRate) / 3) * 1000).toLong())
                    imageResource.intValue = if (imageResource.intValue == R.drawable.tts_rate_0) R.drawable.tts_rate_1 else R.drawable.tts_rate_0
                }
            }
            Image(
                painter = painterResource(id = imageResource.intValue),
                contentDescription = "Speech rate",
                colorFilter = ColorFilter.tint(Color.White),
                modifier = Modifier.size(28.dp)
            )
        }
    }

    suspend fun cast(applicationContext: Context, floatFlow: MutableStateFlow<Float>, position: Point) {
        layoutParams = WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            position.x,
            position.y,
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
                    or WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.TOP or Gravity.START
        }
        this.floatFlow.value = floatFlow
        super.cast(applicationContext)
    }

}

















