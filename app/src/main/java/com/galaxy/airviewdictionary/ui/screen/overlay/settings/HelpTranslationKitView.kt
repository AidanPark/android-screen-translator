package com.galaxy.airviewdictionary.ui.screen.overlay.settings


import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.graphics.PixelFormat
import android.view.Gravity
import android.view.WindowManager
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.net.toUri
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.galaxy.airviewdictionary.R
import com.galaxy.airviewdictionary.core.OverlayService
import com.galaxy.airviewdictionary.data.remote.translation.TranslationKitType
import com.galaxy.airviewdictionary.ui.screen.main.SettingsActivity
import com.galaxy.airviewdictionary.ui.screen.overlay.OverlayView
import com.galaxy.airviewdictionary.ui.screen.overlay.menubar.MenuBarViewModel
import com.galaxy.airviewdictionary.ui.screen.overlay.menubar.TranslationKitIconButton
import javax.inject.Singleton

/**
 * TranslationKit 안내 뷰
 */
@Singleton
class HelpTranslationKitView private constructor() : OverlayView() {

    companion object {
        val INSTANCE: HelpTranslationKitView by lazy(LazyThreadSafetyMode.SYNCHRONIZED) { HelpTranslationKitView() }
    }

    private lateinit var menuBarViewModel: MenuBarViewModel

    private lateinit var initialTranslationKitType: TranslationKitType

    override val layoutParams: WindowManager.LayoutParams = WindowManager.LayoutParams(
        WindowManager.LayoutParams.MATCH_PARENT,
        WindowManager.LayoutParams.MATCH_PARENT,
        WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
        WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
                or WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN
                or WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS
                or WindowManager.LayoutParams.FLAG_DIM_BEHIND
                or WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS,
        PixelFormat.TRANSLUCENT
    ).apply {
        gravity = Gravity.TOP or Gravity.START
//        windowAnimations = android.R.style.Animation_Toast
        dimAmount = 0.85f
    }

    override val composable: @Composable () -> Unit = @Composable {
        if (isAttachedToWindow()) {
            val context = LocalContext.current
            val lifecycleOwner = LocalLifecycleOwner.current
            val configuration = LocalConfiguration.current
            val isPortrait = configuration.orientation == Configuration.ORIENTATION_PORTRAIT

            val translationKitType = remember { mutableStateOf(initialTranslationKitType) }

            if (isPortrait) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            brush = Brush.verticalGradient(
                                colors = listOf(
                                    Color.Black.copy(alpha = 0.0f), // 상단: 투명
                                    Color.Black.copy(alpha = 1f), // 중앙: 불투명
                                    Color.Black.copy(alpha = 0.0f)  // 하단: 투명
                                )
                            )
                        )
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1.35f)
//                            .background(Color(0x4422f4ff))
                        ,
                        contentAlignment = Alignment.Center
                    ) {
                        TranslationKitTypeBox(
                            translationKitType.value,
                            updateTranslationKitType = { _translationKitType ->
                                translationKitType.value = _translationKitType
                            }
                        )
                    }

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(0.65f)
//                            .background(Color(0x44ff44ff))
                        ,
                        contentAlignment = Alignment.TopCenter
                    ) {
                        ProductBox(
                            descriptionResId = when (translationKitType.value) {
                                TranslationKitType.DEEPL -> R.string.help_text_deepl_own_key
                                TranslationKitType.OPENAI -> R.string.help_text_openai_own_key
                                TranslationKitType.GEMINI -> R.string.help_text_gemini_own_key
                                else -> R.string.help_text_no_limit_free
                            }
                        )
                    }
                }
            }

            // landscape
            else {
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black),
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxHeight()
                            .wrapContentWidth()
                            .padding(start = 50.dp)
//                            .background(Color(0x4422f4ff))
                        ,
                        contentAlignment = Alignment.Center
                    ) {
                        TranslationKitTypeBox(
                            translationKitType.value,
                            updateTranslationKitType = { _translationKitType ->
                                translationKitType.value = _translationKitType
                            }
                        )
                    }

                    Box(
                        modifier = Modifier
                            .fillMaxSize()
//                            .background(Color(0x44ff44ff))
                        ,
                        contentAlignment = Alignment.Center
                    ) {
                        ProductBox(
                            descriptionResId = when (translationKitType.value) {
                                TranslationKitType.DEEPL -> R.string.help_text_deepl_own_key
                                TranslationKitType.OPENAI -> R.string.help_text_openai_own_key
                                TranslationKitType.GEMINI -> R.string.help_text_gemini_own_key
                                else -> R.string.help_text_no_limit_free
                            }
                        )
                    }
                }
            }
        }
    }

    @Composable
    fun TranslationKitTypeBox(
        translationKitType: TranslationKitType,
        updateTranslationKitType: (translationKitType: TranslationKitType) -> Unit
    ) {
        val context = LocalContext.current

        Column {
            Row(
                modifier = Modifier.wrapContentSize(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                TranslationKitIconButton(
                    translationKitType = translationKitType,
                    isDarkMode = true,
                    updateTranslationKitType = updateTranslationKitType,
                )
                Text(
                    text = translationKitType.text,
                    color = Color.White,
                    style = MaterialTheme.typography.bodyLarge.copy(fontSize = 18.sp, fontWeight = FontWeight.Bold)
                )
            }
            Box(
                modifier = Modifier
                    .width(300.dp)
                    .height(200.dp)
                    .align(Alignment.CenterHorizontally)
//                    .background(Color(0x4422f4ff))
                ,
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(id = translationKitType.brandResourceId),
                    contentDescription = "$translationKitType brand image",
                    modifier = Modifier
                        .sizeIn(maxHeight = 72.dp),
                    contentScale = ContentScale.Fit
                )
            }

            Row(
                modifier = Modifier
                    .wrapContentSize()
                    .align(Alignment.CenterHorizontally),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Supports  ${menuBarViewModel.translationRepository.getSupportedLanguages(translationKitType).size}  languages",
                    color = Color(0xFFAAAAAA),
                    style = MaterialTheme.typography.bodyLarge.copy(fontSize = 14.sp)
                )
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.OpenInNew,
                    contentDescription = "OpenInNew",
                    tint = Color(0Xff335ef7),
                    modifier = Modifier
                        .size(18.dp)
                        .padding(start = 3.dp)
                        .clickable {
                            val intent = Intent(Intent.ACTION_VIEW, translationKitType.providersUrl.toUri())
                            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                            context.startActivity(intent)
                        }
                )
            }
        }
    }

    suspend fun cast(
        applicationContext: Context,
        initialTranslationKitType: TranslationKitType,
    ) {
        super.cast(applicationContext)
        this.initialTranslationKitType = initialTranslationKitType
    }

    override fun onServiceConnected(overlayService: OverlayService) {
        menuBarViewModel = overlayService.getMenuBarViewModel()
        super.onServiceConnected(overlayService)
    }
}

















