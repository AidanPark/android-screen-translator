package com.galaxy.airviewdictionary.ui.screen.test

import android.content.Context
import android.graphics.PixelFormat
import android.graphics.Rect
import android.view.Gravity
import android.view.WindowManager
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.core.util.TypedValueCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.galaxy.airviewdictionary.core.OverlayService
import com.galaxy.airviewdictionary.data.local.screen.ScreenInfo
import com.galaxy.airviewdictionary.data.local.screen.ScreenInfoHolder
import com.galaxy.airviewdictionary.data.local.vision.model.VisionText
import com.galaxy.airviewdictionary.data.local.vision.model.Word
import com.galaxy.airviewdictionary.extensions.toSp
import com.galaxy.airviewdictionary.ui.screen.overlay.OverlayView
import com.galaxy.airviewdictionary.ui.screen.overlay.targethandle.TargetHandleViewModel


/**
 * com.galaxy.airviewdictionary.model.vision.VisionText 확인용
 */

class TestVisionTextLinesView : OverlayView() {

    companion object {
        val INSTANCE: TestVisionTextLinesView by lazy(LazyThreadSafetyMode.SYNCHRONIZED) { TestVisionTextLinesView() }
    }

    override lateinit var layoutParams: WindowManager.LayoutParams

    private lateinit var targetHandleViewModel: TargetHandleViewModel

    override fun onServiceConnected(overlayService: OverlayService) {
        targetHandleViewModel = overlayService.getTargetHandleViewModel()
        super.onServiceConnected(overlayService)
    }

    override val composable: @Composable () -> Unit = @Composable {
        val visionTextLinesState by targetHandleViewModel.visionResultFlow.collectAsStateWithLifecycle()

        visionTextLinesState?.let { visionTextLines ->
            val displayMetrics = LocalContext.current.resources.displayMetrics
            visionTextLines.paragraphs.forEach { line ->
                val fontSize = TypedValueCompat.pxToDp((line.height * .8).toFloat(), displayMetrics).dp.toSp()
                DrawBoundingBoxAndText(line.boundingBox, line.representation, fontSize)
            }
        }
    }

    override suspend fun cast(applicationContext: Context) {
        val screenInfo: ScreenInfo = ScreenInfoHolder.get()
        layoutParams = WindowManager.LayoutParams(
            screenInfo.width,
            screenInfo.height,
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
                    or WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE
                    or WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN
                    or WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.TOP or Gravity.START
        }

        super.cast(applicationContext)
    }
}

@Composable
private fun DrawVisionText(visionText: VisionText) {
    val displayMetrics = LocalContext.current.resources.displayMetrics
    val width = TypedValueCompat.pxToDp(visionText.width.toFloat(), displayMetrics).dp
    val height = TypedValueCompat.pxToDp(visionText.height.toFloat(), displayMetrics).dp
    Box(
        modifier = Modifier
            .offset { IntOffset(visionText.boundingBox.left, visionText.boundingBox.top) }
            .width(width)
            .height(height)
            .background(Color.Green.copy(alpha = 0.5f))
    ) {
        DrawRelativeBoundingBox(visionText.boundingBox, visionText)
    }
}

@Composable
private fun <T : VisionText> DrawRelativeBoundingBox(parentBoundingBox: Rect, visionText: T) {
    val displayMetrics = LocalContext.current.resources.displayMetrics
    val relativeBoundingBox = visionText.relativeBoundingBox(parentBoundingBox)
    val width = TypedValueCompat.pxToDp(relativeBoundingBox.width().toFloat(), displayMetrics).dp
    val height = TypedValueCompat.pxToDp(relativeBoundingBox.height().toFloat(), displayMetrics).dp
    val fontSize = TypedValueCompat.pxToDp((relativeBoundingBox.height() * .8).toFloat(), displayMetrics).dp.toSp()

    if (visionText is Word) {
        Box(
            modifier = Modifier
                .offset { IntOffset(relativeBoundingBox.left, relativeBoundingBox.top) }
                .width(width)
                .height(height)
                .background(Color.Blue.copy(alpha = 0.5f))
        ) {
            Text(
                text = visionText.representation,
                style = MaterialTheme.typography.bodySmall.copy(color = Color.Black, fontSize = fontSize),
                modifier = Modifier.fillMaxSize(),
                textAlign = TextAlign.Start,
                overflow = TextOverflow.Clip, // 텍스트가 박스를 넘지 않도록 클립
                maxLines = 1 // 한 줄로 표시
            )
        }
    } else {
//        visionText.children?.forEach { child ->
//            DrawRelativeBoundingBox(parentBoundingBox, child)
//        }
    }
}
