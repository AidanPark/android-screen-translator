package com.galaxy.airviewdictionary.ui.screen.test

import android.content.Context
import android.graphics.PixelFormat
import android.graphics.Rect
import android.view.Gravity
import android.view.WindowManager
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.core.util.TypedValueCompat
import com.galaxy.airviewdictionary.core.OverlayService
import com.galaxy.airviewdictionary.data.local.screen.ScreenInfo
import com.galaxy.airviewdictionary.data.local.screen.ScreenInfoHolder
import com.galaxy.airviewdictionary.extensions.isValid
import com.galaxy.airviewdictionary.extensions.toSp
import com.galaxy.airviewdictionary.ui.screen.overlay.OverlayView
import com.galaxy.airviewdictionary.ui.screen.overlay.targethandle.TargetHandleViewModel
import com.google.mlkit.vision.text.Text
import timber.log.Timber


/**
 * com.google.mlkit.vision.text.Text 확인용
 */

class TestMlKitTextView : OverlayView() {

    companion object {
        val INSTANCE: TestMlKitTextView by lazy(LazyThreadSafetyMode.SYNCHRONIZED) { TestMlKitTextView() }
    }

    private lateinit var targetHandleViewModel: TargetHandleViewModel

    override lateinit var layoutParams: WindowManager.LayoutParams

    override val composable: @Composable () -> Unit = @Composable {
//        val mlKitTextState by targetHandleViewModel.analyzedMlKitTextFlow.collectAsStateWithLifecycle()
//
//        mlKitTextState?.let {
//            DrawMlKitText(it)
//        }
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

    override fun onServiceConnected(overlayService: OverlayService) {
        super.onServiceConnected(overlayService)
        targetHandleViewModel = overlayService.getTargetHandleViewModel()
    }
}

@Composable
fun DrawMlKitText(mlKitText: Text) {

    fun averageLineHeight(textBlock: Text.TextBlock): Double {
        var totalHeight = 0.0
        var lineCount = 0
        for (line in textBlock.lines) {
            val lineHeight = line.boundingBox?.height()?.toDouble()
            if (lineHeight != null) {
                totalHeight += lineHeight
                lineCount++
            }
        }
        return if (lineCount > 0) totalHeight / lineCount else 0.0
    }

    mlKitText.textBlocks.forEach { textBlock ->
        if (textBlock.boundingBox.isValid()) {
            // Text.TextBlock
            Timber.tag("TestTextActivity").d("boundingBox ${textBlock.boundingBox} cornerPoints ${textBlock.cornerPoints?.toList()}")
            val displayMetrics = LocalContext.current.resources.displayMetrics
            val fontSize = TypedValueCompat.pxToDp((averageLineHeight(textBlock) * .8).toFloat(), displayMetrics).dp.toSp()
            DrawBoundingBoxAndText(textBlock.boundingBox!!, textBlock.text, fontSize)

            textBlock.lines.forEach { line: Text.Line ->
                if (line.boundingBox.isValid()) {
                    // Text.Line
//                    DrawBoundingBoxAndText(line.boundingBox!!, line.text)

                    line.elements.forEach { element: Text.Element ->
                        if (element.boundingBox.isValid()) {
                            // Text.Element
//                            DrawBoundingBoxAndText(element.boundingBox!!, element.text)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun DrawBoundingBoxAndText(rect: Rect, text: String, fontSize: TextUnit) {
    val displayMetrics = LocalContext.current.resources.displayMetrics
    val width = TypedValueCompat.pxToDp(rect.width().toFloat(), displayMetrics).dp
    val height = TypedValueCompat.pxToDp(rect.height().toFloat(), displayMetrics).dp

    Box(
        modifier = Modifier.offset { IntOffset(rect.left, rect.top) }
//        modifier = Modifier.offset { IntOffset(0, 0) }
    ) {
        Box(
            modifier = Modifier
                .size(width = width, height = height)
//                .size(width =50.dp, height=50.dp )
                .background(Color.Blue.copy(alpha = 0.3f))
        ) {
            Text(
                text = text,
                style = MaterialTheme.typography.bodySmall.copy(color = Color.Black, fontSize = fontSize),
                modifier = Modifier.fillMaxSize(),
                textAlign = TextAlign.Start,
                overflow = TextOverflow.Clip, // 텍스트가 박스를 넘지 않도록 클립
//                maxLines = 1 // 한 줄로 표시
            )
        }
    }
}