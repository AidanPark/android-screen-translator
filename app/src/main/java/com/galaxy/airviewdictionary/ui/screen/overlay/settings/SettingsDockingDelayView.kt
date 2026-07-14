package com.galaxy.airviewdictionary.ui.screen.overlay.settings


import android.content.Context
import android.graphics.PixelFormat
import android.graphics.Point
import android.view.Gravity
import android.view.WindowManager
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AllInclusive
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.galaxy.airviewdictionary.R
import com.galaxy.airviewdictionary.extensions.toPx
import com.galaxy.airviewdictionary.ui.common.fontDimensionResource
import com.galaxy.airviewdictionary.ui.screen.overlay.OverlayView
import kotlinx.coroutines.flow.MutableStateFlow
import javax.inject.Singleton

/**
 * Docking delay sub text 뷰
 */
@Singleton
class SettingsDockingDelayView private constructor() : OverlayView() {

    companion object {
        val INSTANCE: SettingsDockingDelayView by lazy(LazyThreadSafetyMode.SYNCHRONIZED) { SettingsDockingDelayView() }
    }

    override lateinit var layoutParams: WindowManager.LayoutParams

    private val textFlow = mutableStateOf<MutableStateFlow<String>?>(null)

    override val composable: @Composable () -> Unit = @Composable {
        if (isAttachedToWindow() && textFlow.value != null) {
            val text by textFlow.value!!.collectAsStateWithLifecycle()
            Box(
                modifier = Modifier
                    .height(38.dp)
                    .width(80.dp),
                contentAlignment = Alignment.CenterEnd
            ) {
                Box(contentAlignment = Alignment.CenterEnd) {
                    if (text == "15.0 sec") {
                        Icon(
                            imageVector = Icons.Default.AllInclusive,
                            contentDescription = "Docking delay Infinity",
                            modifier = Modifier
                                .size(36.dp)
                                .padding(end = 12.dp),
                            tint = Color.White
                        )
                    } else {
                        Text(
                            modifier = Modifier.padding(end = 6.dp),
                            text = text,
                            color = Color.White,
                            style = MaterialTheme.typography.bodyMedium.copy(fontSize = fontDimensionResource(R.dimen.settings_menu_subtext_size)),
                        )
                    }
                }
            }
        }
    }

    suspend fun cast(applicationContext: Context, textFlow: MutableStateFlow<String>, position: Point) {
        layoutParams = WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            position.x - 80.dp.toPx(applicationContext),
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

















