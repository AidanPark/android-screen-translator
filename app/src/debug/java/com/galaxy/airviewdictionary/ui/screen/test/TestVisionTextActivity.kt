package com.galaxy.airviewdictionary.ui.screen.test

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.os.Build
import android.os.Bundle
import android.view.View
import android.view.WindowManager
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.galaxy.airviewdictionary.data.local.vision.TextDetectMode
import com.galaxy.airviewdictionary.data.local.vision.model.Paragraph
import com.galaxy.airviewdictionary.ui.screen.AVDActivity
import com.google.mlkit.vision.text.Text
import timber.log.Timber


/**
 * 캡처 이미지 확인용
 */
class TestVisionTextActivity : AVDActivity() {

    companion object {
        var capturedBitmap: Bitmap? = null
        var analyzedText: Text? = null
        var analyzedParagraphs: List<Paragraph>? = null
        var textDetectMode: TextDetectMode? = null

        fun start(
            applicationContext: Context,
            capturedBitmap: Bitmap,
            analyzedText: Text?,
            analyzedParagraphs: List<Paragraph>?,
            textDetectMode: TextDetectMode?
        ) {
            Companion.capturedBitmap = capturedBitmap
            Companion.analyzedText = analyzedText
            Companion.analyzedParagraphs = analyzedParagraphs
            Companion.textDetectMode = textDetectMode
            val intent = Intent(applicationContext, TestVisionTextActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            applicationContext.startActivity(intent)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            Surface(modifier = Modifier.fillMaxSize()) {
                capturedBitmap?.let {
                    DrawCapturedBitmap(it)
                    DrawCapturedLabel()
                }
            }
        }

        @Suppress("DEPRECATION")
        window.apply {
            clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
            addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
            decorView.systemUiVisibility =
                View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN or
                    View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR or
                    View.SYSTEM_UI_FLAG_HIDE_NAVIGATION or
                    View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                statusBarColor = android.graphics.Color.TRANSPARENT
            }
        }
    }

    override fun onPause() {
        super.onPause()
        capturedBitmap = null
        finish()
    }
}

@Composable
fun DrawCapturedBitmap(bitmap: Bitmap) {
    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        bitmap.let {
            Timber.tag(TAG).d("bitmap.width ${bitmap.width} bitmap.height ${bitmap.height}")
            Image(
                bitmap = bitmap.asImageBitmap(),
                contentDescription = null,
//                    modifier = Modifier.size(500.dp)
            )
        }
    }
}

@Composable
fun DrawCapturedLabel() {
    Box(
        //   modifier = Modifier.background(Color(0x33ff0000)).size(100.dp)
    ) {
        Text(
            text = "Captured",
            style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold, color = Color.White),
            modifier = Modifier
                .background(Color(0x33ff0000))
                .alpha(0.3f)
                .padding(10.dp)
        )
    }
}
