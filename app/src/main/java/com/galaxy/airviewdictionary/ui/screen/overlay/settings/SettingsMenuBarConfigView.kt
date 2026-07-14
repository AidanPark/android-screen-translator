package com.galaxy.airviewdictionary.ui.screen.overlay.settings


import android.content.Context
import android.graphics.PixelFormat
import android.graphics.Point
import android.view.Gravity
import android.view.WindowManager
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.layout
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.galaxy.airviewdictionary.core.OverlayService
import com.galaxy.airviewdictionary.ui.screen.overlay.OverlayView
import com.galaxy.airviewdictionary.ui.screen.overlay.menubar.MenuBar
import com.galaxy.airviewdictionary.ui.screen.overlay.menubar.MenuConfig
import kotlinx.coroutines.flow.MutableStateFlow
import javax.inject.Singleton

/**
 * Menu bar config 뷰
 */
@Singleton
class SettingsMenuBarConfigView private constructor() : OverlayView() {

    companion object {
        val INSTANCE: SettingsMenuBarConfigView by lazy(LazyThreadSafetyMode.SYNCHRONIZED) { SettingsMenuBarConfigView() }
    }

    private lateinit var viewModel: SliderDialogViewModel

    override lateinit var layoutParams: WindowManager.LayoutParams

    private val textFlow = mutableStateOf<MutableStateFlow<String>?>(null)

    override val composable: @Composable () -> Unit = @Composable {
        if (isAttachedToWindow() && textFlow.value != null) {
            val context = LocalContext.current
            val lifecycleOwner = LocalLifecycleOwner.current
            val configuration = LocalConfiguration.current
            val layoutDirection = LocalLayoutDirection.current
            val isRtl = layoutDirection == LayoutDirection.Rtl

            val text by textFlow.value!!.collectAsStateWithLifecycle()

            // Text detect mode
            val textDetectMode by viewModel.preferenceRepository.textDetectModeFlow.collectAsStateWithLifecycle(
                lifecycle = lifecycleOwner.lifecycle,
                initialValue = null
            )

            // source language
            val sourceLanguageCode by viewModel.preferenceRepository.sourceLanguageCodeFlow.collectAsStateWithLifecycle(
                lifecycle = lifecycleOwner.lifecycle,
                initialValue = "auto"
            )
            val sourceLanguage = viewModel.translationRepository.getSupportedSourceLanguage(sourceLanguageCode)

            // target language
            val targetLanguageCode by viewModel.preferenceRepository.targetLanguageCodeFlow.collectAsStateWithLifecycle(
                lifecycle = lifecycleOwner.lifecycle,
                initialValue = configuration.locales.get(0).language
            )
            val targetLanguage = viewModel.translationRepository.getSupportedTargetLanguage(targetLanguageCode)

            // translationKit Type
            val kitType by viewModel.preferenceRepository.translationKitTypeFlow.collectAsStateWithLifecycle(
                lifecycle = lifecycleOwner.lifecycle,
                initialValue = null
            )

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(240.dp),
//                    .background(Color(0x33aaff22))
            ) {
                val scaleFactor = 0.48f
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(top = 15.dp, bottom = 15.dp, end = 41.dp)
                        .wrapContentSize()
                        .layout { measurable, constraints ->
                            val placeable = measurable.measure(constraints)

                            // 스케일링된 크기 계산
                            val width = (placeable.width * scaleFactor).toInt()
                            val height = (placeable.height * scaleFactor).toInt()

                            layout(width, height) {
                                placeable.placeRelative(0, 0)
                            }
                        }
                ) {
                    if (textDetectMode != null && kitType != null) {
                        MenuBar(
                            menuConfig =
                                when (text) {
                                    MenuConfig.WHOLE.name -> MenuConfig.WHOLE
                                    MenuConfig.LANGUAGE_TRANSLATION_KIT.name -> MenuConfig.LANGUAGE_TRANSLATION_KIT
                                    MenuConfig.DETECT_MODE_LANGUAGE.name -> MenuConfig.DETECT_MODE_LANGUAGE
                                    MenuConfig.LANGUAGE.name -> MenuConfig.LANGUAGE
                                    MenuConfig.WHOLE_SHORT.name -> MenuConfig.WHOLE_SHORT
                                    MenuConfig.LANGUAGE_SHORT_TRANSLATION_KIT.name -> MenuConfig.LANGUAGE_SHORT_TRANSLATION_KIT
                                    MenuConfig.DETECT_MODE_LANGUAGE_SHORT.name -> MenuConfig.DETECT_MODE_LANGUAGE_SHORT
                                    MenuConfig.LANGUAGE_SHORT.name -> MenuConfig.LANGUAGE_SHORT
                                    MenuConfig.DETECT_MODE_TRANSLATION_KIT.name -> MenuConfig.DETECT_MODE_TRANSLATION_KIT
                                    MenuConfig.DETECT_MODE.name -> MenuConfig.DETECT_MODE
                                    MenuConfig.TRANSLATION_KIT.name -> MenuConfig.TRANSLATION_KIT

                                    MenuConfig.V_DETECT_MODE.name -> MenuConfig.V_DETECT_MODE
                                    MenuConfig.V_DETECT_MODE_TRANSLATION_KIT.name -> MenuConfig.V_DETECT_MODE_TRANSLATION_KIT
                                    MenuConfig.V_LANGUAGE.name -> MenuConfig.V_LANGUAGE
                                    MenuConfig.V_LANGUAGE_TRANSLATION_KIT.name -> MenuConfig.V_LANGUAGE_TRANSLATION_KIT
                                    MenuConfig.V_DETECT_MODE_LANGUAGE.name -> MenuConfig.V_DETECT_MODE_LANGUAGE
                                    MenuConfig.V_WHOLE.name -> MenuConfig.V_WHOLE
                                    else -> MenuConfig.WHOLE
                                },
                            scaleFactor = scaleFactor,
                            shadowPadding = 0.dp,
                            borderWidth = 1.2.dp,
                            textDetectMode = textDetectMode!!,
                            sourceLanguageCode = sourceLanguageCode,
                            sourceLanguage = sourceLanguage,
                            targetLanguageCode = targetLanguageCode,
                            targetLanguage = targetLanguage,
                            translationKitType = kitType!!,
                            isSwappable = { sourceLanguageCode, targetLanguageCode, kitType ->
                                viewModel.isLanguageSwappable(sourceLanguageCode, targetLanguageCode, kitType)
                            },
                            modifier = Modifier.semantics {
                                contentDescription = "Menu composition"
                            }
                        )
                    }
                }
            }
        }
    }

    override fun onServiceConnected(overlayService: OverlayService) {
        viewModel = overlayService.getSliderDialogViewModel()
        super.onServiceConnected(overlayService)
    }

    suspend fun cast(applicationContext: Context, textFlow: MutableStateFlow<String>, position: Point) {
        layoutParams = WindowManager.LayoutParams(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            0,
            position.y,
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
                    or WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN
                    or WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.TOP or Gravity.START
        }
        this.textFlow.value = textFlow
        super.cast(applicationContext)
    }

}

















