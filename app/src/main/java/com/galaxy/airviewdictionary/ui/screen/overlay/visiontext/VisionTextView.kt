package com.galaxy.airviewdictionary.ui.screen.overlay.visiontext

import android.content.Context
import android.graphics.PixelFormat
import android.view.Gravity
import android.view.MotionEvent
import android.view.WindowManager
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.util.TypedValueCompat
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.galaxy.airviewdictionary.R
import com.galaxy.airviewdictionary.core.OverlayService
import com.galaxy.airviewdictionary.data.local.vision.TextDetectMode
import com.galaxy.airviewdictionary.data.local.vision.WritingDirection
import com.galaxy.airviewdictionary.data.local.vision.model.Line
import com.galaxy.airviewdictionary.data.local.vision.model.Paragraph
import com.galaxy.airviewdictionary.data.local.vision.model.Sentence
import com.galaxy.airviewdictionary.data.local.vision.model.VisionText
import com.galaxy.airviewdictionary.data.local.vision.model.Word
import com.galaxy.airviewdictionary.ui.screen.overlay.OverlayView
import com.galaxy.airviewdictionary.ui.screen.overlay.targethandle.TargetHandleViewModel
import timber.log.Timber
import javax.inject.Singleton


/**
 * 포인터가 머무는 위치의 VisionText 뷰
 */
@Singleton
class VisionTextView private constructor() : OverlayView() {

    companion object {
        val INSTANCE: VisionTextView by lazy(LazyThreadSafetyMode.SYNCHRONIZED) { VisionTextView() }

        var paragraphFrameMargin: Int = 0
    }

    private lateinit var targetHandleViewModel: TargetHandleViewModel

    override lateinit var layoutParams: WindowManager.LayoutParams

    override val composable: @Composable () -> Unit = @Composable {
        val lifecycleOwner = LocalLifecycleOwner.current

        val motionEventState by targetHandleViewModel.motionEventFlow.collectAsStateWithLifecycle()
        val pointerPositionedVisionTextState by targetHandleViewModel.pointerPositionedVisionTextFlow.collectAsStateWithLifecycle(
            lifecycle = lifecycleOwner.lifecycle,
            initialValue = null
        )

        val textDetectMode by targetHandleViewModel.preferenceRepository.textDetectModeFlow.collectAsStateWithLifecycle(
            lifecycle = lifecycleOwner.lifecycle,
            initialValue = TextDetectMode.SENTENCE
        )

        pointerPositionedVisionTextState?.let { visionText ->
//            Timber.tag(TAG).d("${visionText.boundingBox}")
            when (visionText) {
                is Paragraph -> Timber.tag(TAG).d("Paragraph ${visionText.representation}")
                is Line -> Timber.tag(TAG).d("Line ${visionText.representation}")
                is Word -> Timber.tag(TAG).d("Word ${visionText.representation}")
            }

            updateLayoutParams(LocalContext.current, visionText)
            updateLayout(LocalContext.current)
            VisionTextBox(visionText, textDetectMode)
        }

        if (motionEventState == MotionEvent.ACTION_UP) {
            clear()
        }
    }

    suspend fun cast(applicationContext: Context, visionText: VisionText) {
        updateLayoutParams(applicationContext, visionText)
        super.cast(applicationContext)
    }

    private fun updateLayoutParams(context: Context, visionText: VisionText) {
        paragraphFrameMargin = if (visionText is Paragraph) context.resources.getDimensionPixelSize(R.dimen.visiontext_view_paragraph_frame_margin) else 0
        layoutParams = WindowManager.LayoutParams(
            visionText.width + paragraphFrameMargin * 2,
            visionText.height + paragraphFrameMargin * 2,
            visionText.boundingBox.left - paragraphFrameMargin,
            visionText.boundingBox.top - paragraphFrameMargin,
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
//                    or WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE 이 옵션을 사용하면 배경에 강제로 alpha 가 추가됨
                    or WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN
                    or WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.TOP or Gravity.START
        }
    }

    override fun onServiceConnected(overlayService: OverlayService) {
        targetHandleViewModel = overlayService.getTargetHandleViewModel()
        super.onServiceConnected(overlayService)
    }
}

@Composable
fun VisionTextBox(
    visionText: VisionText,
    textDetectMode: TextDetectMode
) {
    val displayMetrics = LocalContext.current.resources.displayMetrics
    val textWidth = TypedValueCompat.pxToDp(visionText.width.toFloat(), displayMetrics).dp
    val textHeight = TypedValueCompat.pxToDp(visionText.height.toFloat(), displayMetrics).dp
    val isDarkMode = isSystemInDarkTheme()
    val isRtl = LocalLayoutDirection.current == LayoutDirection.Rtl
    val visionTextColor = colorResource(if (isDarkMode) R.color.vision_text_color_dark else R.color.vision_text_color)
    val alpha = when {
        textDetectMode == TextDetectMode.SELECT && isDarkMode -> 0.24f
        textDetectMode == TextDetectMode.SELECT && !isDarkMode -> 0.16f
        isDarkMode -> 0.38f
        else -> 0.2f
    }

    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        if (visionText is Sentence) {
            visionText.lines.forEach {
                val lineWidth = TypedValueCompat.pxToDp(it.width.toFloat(), displayMetrics).dp
                val lineHeight = TypedValueCompat.pxToDp(it.height.toFloat(), displayMetrics).dp
                val relativeBoundingBox = it.relativeBoundingBox(visionText.boundingBox)
                val paddingStart = when (visionText.writingDirection) {
                    WritingDirection.LTR, WritingDirection.TTB_LTR -> TypedValueCompat.pxToDp(relativeBoundingBox.left.toFloat(), displayMetrics).dp
                    WritingDirection.RTL, WritingDirection.TTB_RTL -> TypedValueCompat.pxToDp(relativeBoundingBox.right.toFloat(), displayMetrics).dp
                }
                val paddingTop = TypedValueCompat.pxToDp(relativeBoundingBox.top.toFloat(), displayMetrics).dp
//                Timber.tag("VisionTextView").i("Sentence ${it.boundingBox} ${it.representation} $paddingStart, $paddingTop $lineWidth, $lineHeight")

                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(
                            start = paddingStart,
                            top = paddingTop,
                        )
                ) {
                    Text(
//                        text = it.representation,
                        text = "",
                        style = MaterialTheme.typography.bodySmall.copy(color = Color.Black, fontSize = 11.sp),
                        modifier = Modifier
                            .align(Alignment.TopStart)
                            .background(visionTextColor.copy(alpha = alpha))
                            .width(lineWidth)
                            .height(lineHeight),
                        textAlign = TextAlign.Start,
                        overflow = TextOverflow.Clip,
                    )
                }
            }
        } else {
            Text(
//                text = visionText.representation,
                text = "",
                style = MaterialTheme.typography.bodySmall.copy(color = Color.Black, fontSize = 11.sp),
                modifier = Modifier
                    .align(Alignment.Center)
                    .background(visionTextColor.copy(alpha = alpha))
                    .width(textWidth)
                    .height(textHeight),
                textAlign = TextAlign.Start,
                overflow = TextOverflow.Clip,
            )
        }

        // Paragraph 인 경우 시작과 끝에 모서리 이미지 표시
        if (visionText is Paragraph) {
            Image(
                painter = painterResource(id = R.drawable.paragraph_window_top_left),
                contentDescription = null,
                modifier = Modifier
                    .size(12.dp)
                    .align(Alignment.TopStart)
                    .alpha(0.7f)
                    .graphicsLayer {
                        if (isRtl) rotationY = 180f
                    },
                colorFilter = ColorFilter.tint(colorResource(R.color.imageview_tint_color_enable))
            )
            Image(
                painter = painterResource(id = R.drawable.paragraph_window_top_right),
                contentDescription = null,
                modifier = Modifier
                    .size(12.dp)
                    .align(Alignment.TopEnd)
                    .alpha(0.7f)
                    .graphicsLayer {
                        if (isRtl) rotationY = 180f
                    },
                colorFilter = ColorFilter.tint(colorResource(R.color.imageview_tint_color_enable))
            )
            Image(
                painter = painterResource(id = R.drawable.paragraph_window_bottom_left),
                contentDescription = null,
                modifier = Modifier
                    .size(12.dp)
                    .align(Alignment.BottomStart)
                    .alpha(0.7f)
                    .graphicsLayer {
                        if (isRtl) rotationY = 180f
                    },
                colorFilter = ColorFilter.tint(colorResource(R.color.imageview_tint_color_enable))
            )
            Image(
                painter = painterResource(id = R.drawable.paragraph_window_bottom_right),
                contentDescription = null,
                modifier = Modifier
                    .size(12.dp)
                    .align(Alignment.BottomEnd)
                    .alpha(0.7f)
                    .graphicsLayer {
                        if (isRtl) rotationY = 180f
                    },
                colorFilter = ColorFilter.tint(colorResource(R.color.imageview_tint_color_enable))
            )
        }
    }

}

