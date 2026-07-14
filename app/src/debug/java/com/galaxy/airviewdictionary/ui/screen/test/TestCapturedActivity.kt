package com.galaxy.airviewdictionary.ui.screen.test

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.os.Build
import android.os.Bundle
import android.view.View
import android.view.WindowManager
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.galaxy.airviewdictionary.ui.screen.AVDActivity


/**
 * 캡처 이미지 확인용
 */
class TestCapturedActivity : AVDActivity() {

    companion object {
        var capturedBitmap: Bitmap? = null

        fun start(applicationContext: Context, capturedBitmap: Bitmap) {
            Companion.capturedBitmap = capturedBitmap
            val intent = Intent(applicationContext, TestCapturedActivity::class.java)
            intent.addFlags (Intent.FLAG_ACTIVITY_NO_ANIMATION)
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

//@Composable
//fun DrawCapturedBitmap(bitmap: Bitmap) {
//    Box(
//        modifier = Modifier.fillMaxSize()
//    ) {
//        bitmap.let {
//            Timber.tag(TAG).d("bitmap.width ${bitmap.width} bitmap.height ${bitmap.height}")
//            Image(
//                bitmap = bitmap.asImageBitmap(),
//                contentDescription = null,
////                    modifier = Modifier.size(500.dp)
//            )
//        }
//    }
//}
//
//@Composable
//fun DrawCapturedLabel() {
//    Box(
//        //   modifier = Modifier.background(Color(0x33ff0000)).size(100.dp)
//    ) {
//        Text(
//            text = "Captured",
//            style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold, color = Color.White),
//            modifier = Modifier
//                .background(Color(0x33ff0000))
//                .alpha(0.3f)
//                .padding(10.dp)
//        )
//    }
//}
