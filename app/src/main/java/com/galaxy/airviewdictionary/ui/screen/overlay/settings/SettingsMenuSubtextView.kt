package com.galaxy.airviewdictionary.ui.screen.overlay.settings


import android.content.Context
import android.graphics.PixelFormat
import android.graphics.Point
import android.view.Gravity
import android.view.WindowManager
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.galaxy.airviewdictionary.R
import com.galaxy.airviewdictionary.extensions.toPx
import com.galaxy.airviewdictionary.ui.common.fontDimensionResource
import com.galaxy.airviewdictionary.ui.screen.overlay.OverlayView
import kotlinx.coroutines.flow.MutableStateFlow
import javax.inject.Singleton

/**
 * 메뉴 sub text 뷰
 */
@Singleton
class SettingsMenuSubtextView private constructor() : OverlayView() {

    companion object {
        val INSTANCE: SettingsMenuSubtextView by lazy(LazyThreadSafetyMode.SYNCHRONIZED) { SettingsMenuSubtextView() }
    }

    override lateinit var layoutParams: WindowManager.LayoutParams

    private val textFlow = mutableStateOf<MutableStateFlow<String>?>(null)

    override val composable: @Composable () -> Unit = @Composable {
        if (isAttachedToWindow() && textFlow.value != null) {
            val context = LocalContext.current
            val layoutDirection = LocalLayoutDirection.current
            val isRtl = layoutDirection == LayoutDirection.Rtl
            val text by textFlow.value!!.collectAsStateWithLifecycle()
            val fontSize = fontDimensionResource(R.dimen.settings_menu_subtext_size)

            Box(
                modifier = Modifier
                    .width(60.dp)
                //    .background(Color(0x1117fa23))
                ,
                contentAlignment = if (isRtl) Alignment.CenterStart else Alignment.CenterEnd
            ) {
                Text(
                    text = text,
                    color = Color.White,
                    style = MaterialTheme.typography.bodyMedium.copy(fontSize = fontSize),
                )
            }
        }
    }

    suspend fun cast(applicationContext: Context, textFlow: MutableStateFlow<String>, position: Point) {
        layoutParams = WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            position.x - 60.dp.toPx(applicationContext),
            position.y,
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
                    or WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.TOP or Gravity.START
        }
        this.textFlow.value = textFlow
        super.cast(applicationContext)
    }

}

















