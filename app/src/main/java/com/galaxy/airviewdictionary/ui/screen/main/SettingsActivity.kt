package com.galaxy.airviewdictionary.ui.screen.main


import android.annotation.SuppressLint
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.graphics.Point
import android.os.Build
import android.os.Bundle
import android.os.StrictMode
import android.os.StrictMode.ThreadPolicy
import android.view.WindowInsets
import androidx.activity.OnBackPressedCallback
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.Crossfade
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionLayout
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AllInclusive
import androidx.compose.material.icons.filled.FiberNew
import androidx.compose.material.icons.filled.PowerSettingsNew
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.filled.VoiceChat
import androidx.compose.material.icons.outlined.CardGiftcard
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Movie
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.LayoutCoordinates
import androidx.compose.ui.layout.boundsInRoot
import androidx.compose.ui.layout.boundsInWindow
import androidx.compose.ui.layout.layout
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionOnScreen
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.graphics.toColorInt
import androidx.core.view.WindowCompat
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.lifecycleScope
import com.android.billingclient.api.Purchase
import com.galaxy.airviewdictionary.BuildConfig
import com.galaxy.airviewdictionary.R
import com.galaxy.airviewdictionary.data.local.capture.CaptureRepository
import com.galaxy.airviewdictionary.data.local.screen.ScreenInfoHolder
import com.galaxy.airviewdictionary.data.local.secure.TrialLimitInfo
import com.galaxy.airviewdictionary.data.local.vision.TextDetectMode
import com.galaxy.airviewdictionary.data.remote.ai.CorrectionKitType
import com.galaxy.airviewdictionary.data.remote.firebase.RemoteConfigRepository
import com.galaxy.airviewdictionary.data.remote.translation.TranslationKitType
import com.galaxy.airviewdictionary.extensions.finishService
import com.galaxy.airviewdictionary.extensions.gotoStore
import com.galaxy.airviewdictionary.extensions.toPx
import com.galaxy.airviewdictionary.extensions.vibrate
import com.galaxy.airviewdictionary.ui.common.AutoRefreshEveryMinute
import com.galaxy.airviewdictionary.ui.common.fontDimensionResource
import com.galaxy.airviewdictionary.ui.screen.AVDActivity
import com.galaxy.airviewdictionary.ui.screen.intro.SplashActivity
import com.galaxy.airviewdictionary.ui.screen.overlay.dialog.DialogView
import com.galaxy.airviewdictionary.ui.screen.overlay.languagelist.LanguageListView
import com.galaxy.airviewdictionary.ui.screen.overlay.menubar.MenuBar
import com.galaxy.airviewdictionary.ui.screen.overlay.menubar.MenuBarView
import com.galaxy.airviewdictionary.ui.screen.overlay.menubar.MenuConfig
import com.galaxy.airviewdictionary.ui.screen.overlay.settings.HelpTextDetectModeView
import com.galaxy.airviewdictionary.ui.screen.overlay.settings.HelpTranslationKitView
import com.galaxy.airviewdictionary.ui.screen.overlay.settings.SliderDialogView
import com.galaxy.airviewdictionary.ui.screen.overlay.targethandle.TargetHandleView
import com.galaxy.airviewdictionary.ui.screen.overlay.voicelist.VoiceListView
import com.galaxy.airviewdictionary.ui.screen.permissions.ScreenCapturePermissionRequesterActivity
import com.galaxy.airviewdictionary.ui.theme.ScreenTranslatorTheme
import com.google.android.play.core.review.ReviewException
import com.google.android.play.core.review.ReviewManager
import com.google.android.play.core.review.ReviewManagerFactory
import com.google.android.play.core.review.testing.FakeReviewManager
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import timber.log.Timber
import java.math.BigDecimal
import java.math.RoundingMode
import kotlin.math.ceil
import kotlin.math.round
import kotlin.math.roundToInt


@AndroidEntryPoint
class SettingsActivity : AVDActivity() {

    companion object {

        const val EXTRA_PURCHASE = "EXTRA_PURCHASE"

        const val EXTRA_PURCHASE_INDUCEMENT = "EXTRA_PURCHASE_INDUCEMENT"

        fun start(context: Context) {
            val intent = Intent(context, SettingsActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(intent)
        }

        fun purchaseInduce(context: Context) {
            val intent = Intent(context, SettingsActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            intent.putExtra(EXTRA_PURCHASE_INDUCEMENT, true)
            context.startActivity(intent)
        }

        fun purchase(context: Context) {
            val intent = Intent(context, SettingsActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            intent.putExtra(EXTRA_PURCHASE, true)
            context.startActivity(intent)
        }

        val liveStateFlow = MutableStateFlow(false)

        val menuBarViewSettlePositionFlow = MutableStateFlow<Point?>(null)

        val premiumViewVisibleStateFlow = MutableStateFlow(false)
    }

    private val viewModel: SettingsViewModel by viewModels()

    private val settingFloatFlow = MutableStateFlow(1.0f)

    private val settingStringFlow = MutableStateFlow("")

//    private val snackMessageFlow = MutableStateFlow("")

    @OptIn(ExperimentalSharedTransitionApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        ScreenInfoHolder.collectAndStoreScreenInfo(this)

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
            val policy = ThreadPolicy.Builder().permitAll().build()
            StrictMode.setThreadPolicy(policy)
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            val isDarkMode = (resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK) == Configuration.UI_MODE_NIGHT_YES
            val colorWhite = "#FFf2f1f4".toColorInt()
            val colorBlack = "#FF010102".toColorInt()
            window.decorView.setOnApplyWindowInsetsListener { view, insets ->
                val statusBarInsets = insets.getInsets(WindowInsets.Type.statusBars())
                view.setBackgroundColor(if (isDarkMode) colorBlack else colorWhite)
                view.setPadding(0, statusBarInsets.top, 0, 0)
                insets
            }
            WindowCompat.getInsetsController(window, window.decorView).isAppearanceLightStatusBars = !isDarkMode
            WindowCompat.getInsetsController(window, window.decorView).isAppearanceLightNavigationBars = !isDarkMode
        }

        val purchaseInducement = intent.getBooleanExtra(EXTRA_PURCHASE_INDUCEMENT, false)
        premiumViewVisibleStateFlow.value = purchaseInducement

        setContent {
            ScreenTranslatorTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    val lifecycleOwner = LocalLifecycleOwner.current

                    val premiumViewVisible by premiumViewVisibleStateFlow.collectAsStateWithLifecycle(
                        lifecycle = lifecycleOwner.lifecycle,
                        initialValue = false
                    )

                    val isDarkMode = isSystemInDarkTheme()
                    val backgroundColor = if (isDarkMode) Color(0xFF010102) else Color(0xFFf2f1f4)

                    val snackBarHostState = remember { SnackbarHostState() }

                    LaunchedEffect(viewModel.billingRepository.purchaseStateMessageFlow) {
                        viewModel.billingRepository.purchaseStateMessageFlow.collect { message ->
                            if (message.isNotEmpty()) {
                                lifecycleScope.launch {
                                    snackBarHostState.showSnackbar(
                                        message = message,
                                        duration = SnackbarDuration.Long,
//                                        actionLabel = "snackbar"
                                    )
                                }
                            }
                        }
                    }

//                    LaunchedEffect(snackMessageFlow) {
//                        snackMessageFlow.collect { message ->
//                            if (message.isNotEmpty()) {
//                                lifecycleScope.launch {
//                                    snackBarHostState.showSnackbar(
//                                        message = message,
//                                        duration = SnackbarDuration.Short,
////                                        actionLabel = "snackbar"
//                                    )
//                                }
//                            }
//                        }
//                    }

                    Scaffold(
                        snackbarHost = { SnackbarHost(hostState = snackBarHostState) }
                    ) { _paddingValues: PaddingValues ->
                        val paddingValues = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                            PaddingValues()
                        } else {
                            _paddingValues
                        }
                        Box(
                            modifier = Modifier
                                .background(backgroundColor)
                                .padding(paddingValues)
                        ) {
                            SharedTransitionLayout {
                                AnimatedContent(
                                    targetState = premiumViewVisible,
                                    label = "menu_premium_transition",
                                ) { showPremium ->
                                    if (showPremium) {
                                        PremiumView(
                                            onBack = {
                                                premiumViewVisibleStateFlow.value = false
                                            },
                                            animatedVisibilityScope = this@AnimatedContent,
                                            sharedTransitionScope = this@SharedTransitionLayout
                                        )
                                    } else {
                                        Settings(
                                            onShowPremium = {
                                                premiumViewVisibleStateFlow.value = true
                                            },
                                            animatedVisibilityScope = this@AnimatedContent,
                                            sharedTransitionScope = this@SharedTransitionLayout,
                                            paddingValues = paddingValues
                                        )
                                    }
                                }
                            }
                        }
                    }

                }
            }
        }

        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (LanguageListView.INSTANCE.isRunning.get()) {
                    LanguageListView.INSTANCE.clear()
                } else if (HelpTextDetectModeView.INSTANCE.isRunning.get()) {
                    HelpTextDetectModeView.INSTANCE.clear()
                } else if (HelpTranslationKitView.INSTANCE.isRunning.get()) {
                    HelpTranslationKitView.INSTANCE.clear()
                } else if (SliderDialogView.INSTANCE.isRunning.get()) {
                    closeTranslation()
                    SliderDialogView.INSTANCE.clear()
                } else if (VoiceListView.INSTANCE.isRunning.get()) {
                    VoiceListView.INSTANCE.clear()
                } else {
                    isEnabled = false
                    onBackPressedDispatcher.onBackPressed()
                }
            }
        })

        appReview()
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        if (hasFocus && Build.VERSION.SDK_INT < Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            val isDarkMode = (resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK) == Configuration.UI_MODE_NIGHT_YES
            val colorWhite = android.graphics.Color.parseColor("#FFf2f1f4")
            val colorBlack = android.graphics.Color.parseColor("#FF010102")
            window.statusBarColor = if (isDarkMode) colorBlack else colorWhite
            window.navigationBarColor = if (isDarkMode) colorBlack else colorWhite
        }
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        premiumViewVisibleStateFlow.value = intent?.getBooleanExtra(EXTRA_PURCHASE_INDUCEMENT, false) == true

        if (intent?.getBooleanExtra(EXTRA_PURCHASE, false) == true) {
            viewModel.launchBillingFlow(this)
        }
    }

    override fun onResume() {
        super.onResume()

        val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        val areNotificationsEnabled = Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU || notificationManager.areNotificationsEnabled()
        val canDrawOverlays = android.provider.Settings.canDrawOverlays(applicationContext)

        if (!areNotificationsEnabled || !canDrawOverlays) {
            val intent = Intent(applicationContext, SplashActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(intent)
            finish()
            return
        }

        liveStateFlow.value = true

        lifecycleScope.launch {
            if (!MenuBarView.INSTANCE.isRunning.get()) {
                MenuBarView.INSTANCE.cast(applicationContext)
            }
            if (!TargetHandleView.INSTANCE.isRunning.get()) {
                TargetHandleView.INSTANCE.cast(applicationContext)
//                snackMessageFlow.value = getString(R.string.snack_message_start_foreground_service)
            }

            delay(1000)
            if (isActive) {
                if (CaptureRepository.mediaProjectionToken == null) {
                    // 화면 캡처 권한을 요청
                    val intent = Intent(this@SettingsActivity, ScreenCapturePermissionRequesterActivity::class.java)
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    startActivity(intent)
                }
            }
        }
    }

    override fun onPause() {
        closeTranslation()
        LanguageListView.INSTANCE.clear()
        SliderDialogView.INSTANCE.clear()
        VoiceListView.INSTANCE.clear()
        HelpTextDetectModeView.INSTANCE.clear()
        HelpTranslationKitView.INSTANCE.clear()
        premiumViewVisibleStateFlow.value = false
        liveStateFlow.value = false
        super.onPause()
    }

    private var _textDetectMode: TextDetectMode? = null

    private fun runTranslation(point: Point, textDetectMode: TextDetectMode) {
        _textDetectMode = textDetectMode
        TargetHandleView.INSTANCE.runTranslation(point, textDetectMode)
    }

    private fun closeTranslation() {
        TargetHandleView.INSTANCE.closeTranslation(_textDetectMode)
    }

    enum class MenuItemPosition {
        Single,
        Top,
        Middle,
        Bottom,
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////
    //                                                                                            //
    //                                          Composable                                        //
    //                                                                                            //
    ////////////////////////////////////////////////////////////////////////////////////////////////
    @SuppressLint("LocalContextConfigurationRead")
    @OptIn(ExperimentalSharedTransitionApi::class)
    @Composable
    fun Settings(
        onShowPremium: () -> Unit,
        sharedTransitionScope: SharedTransitionScope,
        animatedVisibilityScope: AnimatedVisibilityScope,
        paddingValues: PaddingValues,
    ) {
        val context = LocalContext.current
        val localView = LocalView.current
        val lifecycleOwner = LocalLifecycleOwner.current
        val coroutineScope = rememberCoroutineScope()

        val packageInfo = context.packageManager.getPackageInfo(context.packageName, 0)
        val versionCode: Long = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            packageInfo.longVersionCode
        } else {
            packageInfo.versionCode.toLong()
        }

        val latestVersionCode by viewModel.latestVersionCodeFlow.collectAsStateWithLifecycle(
            lifecycle = lifecycleOwner.lifecycle,
            initialValue = 0
        )

        val layoutDirection = LocalLayoutDirection.current
        val isRtl = layoutDirection == LayoutDirection.Rtl
        val configuration = LocalConfiguration.current
        val isPortrait = configuration.orientation == Configuration.ORIENTATION_PORTRAIT

        val menuExpandAnimationDuration = 300

        val contentPadding = 9.dp
        val cornerRound = 32.dp
        val startPadding = paddingValues.calculateLeftPadding(layoutDirection).toPx(context)

        val isDarkMode = isSystemInDarkTheme()
        val contentColor = if (isDarkMode) Color(0xFFfcfcfc) else Color(0xFF010000)
        val subContentColor = if (isDarkMode) Color(0xFFb7b7ba) else Color(0xFF626265)
        val switchScale = 0.70f
        val switchThumbColor = if (isDarkMode) Color.Black else Color.White
        val switchTrackColor = if (isDarkMode) Color(0xFF6a91b2) else Color(0xFF446987)
        val dividerColor = if (isDarkMode) Color(0xFF343434) else Color(0xFFd5d5d5)
        val buttonColor = if (isDarkMode) Color(0xFFfafafa) else Color(0xFF171717)

        // Pointer docking delay
        val dockingDelayTextOffset = remember { mutableStateOf(Point(0, 0)) }
        val dockingDelaySubtextOffset = remember { mutableStateOf(Point(0, 0)) }
        val dockingDelay by viewModel.preferenceRepository.dockingDelayFlow.collectAsStateWithLifecycle(
            lifecycle = lifecycleOwner.lifecycle,
            initialValue = 3000L
        )

        // Haptic feedback to detection
        val dragHandleHaptic by viewModel.preferenceRepository.dragHandleHapticFlow.collectAsStateWithLifecycle(
            lifecycle = lifecycleOwner.lifecycle,
            initialValue = false
        )

        // Menubar Visibility
        val menuBarVisibility by viewModel.preferenceRepository.menuBarVisibilityFlow.collectAsStateWithLifecycle(
            lifecycle = lifecycleOwner.lifecycle,
            initialValue = true
        )

        // Menubar transparency
        val menuBarTransparencyTextOffset = remember { mutableStateOf(Point(0, 0)) }
        val menuBarTransparencySubtextOffset = remember { mutableStateOf(Point(0, 0)) }
        val menuBarTransparency by viewModel.preferenceRepository.menuBarTransparencyFlow.collectAsStateWithLifecycle(
            lifecycle = lifecycleOwner.lifecycle,
            initialValue = 1.0f
        )

        // Menubar Composition
        val menuBarConfigTextOffset = remember { mutableStateOf(Point(0, 0)) }
        val menuBarConfigSubOffset = remember { mutableStateOf(Point(0, 0)) }
        val menuBarConfig by viewModel.preferenceRepository.menuBarConfigFlow.collectAsStateWithLifecycle(
            lifecycle = lifecycleOwner.lifecycle,
            initialValue = MenuConfig.WHOLE
        )

        // Translation transparency
        val translationTransparencyTextOffset = remember { mutableStateOf(Point(0, 0)) }
        val translationTransparencySubtextOffset = remember { mutableStateOf(Point(0, 0)) }
        val translationPoint = remember { mutableStateOf(Point(0, 0)) }
        val translationTransparency by viewModel.preferenceRepository.translationTransparencyFlow.collectAsStateWithLifecycle(
            lifecycle = lifecycleOwner.lifecycle,
            initialValue = 1.0f
        )

        // Translation close delay
        val translationCloseDelayTextOffset = remember { mutableStateOf(Point(0, 0)) }
        val translationCloseDelaySubtextOffset = remember { mutableStateOf(Point(0, 0)) }
        val translationCloseDelay by viewModel.preferenceRepository.translationCloseDelayFlow.collectAsStateWithLifecycle(
            lifecycle = lifecycleOwner.lifecycle,
            initialValue = 1600L
        )

        // Reply transparency
        val replyTransparencyTextOffset = remember { mutableStateOf(Point(0, 0)) }
        val replyTransparencySubtextOffset = remember { mutableStateOf(Point(0, 0)) }
        val replyTransparency by viewModel.preferenceRepository.replyTransparencyFlow.collectAsStateWithLifecycle(
            lifecycle = lifecycleOwner.lifecycle,
            initialValue = 1.0f
        )

        // AI text correction
        val useCorrectionKit by viewModel.preferenceRepository.useCorrectionKitFlow.collectAsStateWithLifecycle(
            lifecycle = lifecycleOwner.lifecycle,
            initialValue = false
        )

        val correctionKit by viewModel.preferenceRepository.correctionKitTypeFlow.collectAsStateWithLifecycle(
            lifecycle = lifecycleOwner.lifecycle,
            initialValue = CorrectionKitType.CHAT_GPT
        )

        // Automatic translation playback
        val automaticTranslationPlayback by viewModel.preferenceRepository.automaticTranslationPlaybackFlow.collectAsStateWithLifecycle(
            lifecycle = lifecycleOwner.lifecycle,
            initialValue = false
        )

        // TTS Speech rate
        val ttsSpeechRateTextOffset = remember { mutableStateOf(Point(0, 0)) }
        val ttsSpeechRateIconOffset = remember { mutableStateOf(Point(0, 0)) }
        val ttsSpeechRate by viewModel.preferenceRepository.ttsSpeechRateFlow.collectAsStateWithLifecycle(
            lifecycle = lifecycleOwner.lifecycle,
            initialValue = 1.0f
        )

        // TTS Voices
        val ttsAvailableVoices by viewModel.ttsRepository.availableVoicesFlow.collectAsStateWithLifecycle(
            lifecycle = lifecycleOwner.lifecycle,
            initialValue = emptyList()
        )

        // TTS Voice
        val ttsCurrentVoice by viewModel.ttsRepository.currentVoiceFlow.collectAsStateWithLifecycle(
            lifecycle = lifecycleOwner.lifecycle,
            initialValue = null
        )

        // Text detect mode
        val textDetectMode by viewModel.preferenceRepository.textDetectModeFlow.collectAsStateWithLifecycle(
            lifecycle = lifecycleOwner.lifecycle,
            initialValue = TextDetectMode.SENTENCE
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
            initialValue = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                context.resources.configuration.locales.get(0).language
            } else {
                @Suppress("DEPRECATION")
                context.resources.configuration.locale.language
            }
        )
        val targetLanguage = viewModel.translationRepository.getSupportedTargetLanguage(targetLanguageCode)

        // translationKit Type
        val kitType by viewModel.preferenceRepository.translationKitTypeFlow.collectAsStateWithLifecycle(
            lifecycle = lifecycleOwner.lifecycle,
            initialValue = TranslationKitType.GOOGLE
        )

        fun getTransparencyValueText(transparency: Float): String {
            return "${ceil((1.0f - transparency) * 100).toInt()}%"
        }

        fun getSecondValueText(second: Long): String {
            return "${round(second / 1000.0 * 10) / 10} sec"
        }

        val onPremiumTreeTrialTextResource = remember { mutableIntStateOf(R.string.settings_menu_on_premium_free_trial) }

        val remoteConfig by viewModel.remoteConfigRepository.remoteConfigFlow.collectAsStateWithLifecycle(
            lifecycle = lifecycleOwner.lifecycle,
            initialValue = emptyMap()
        )
        LaunchedEffect(remoteConfig) {
            Timber.tag(TAG).d("trialRemainMinutes ${TrialLimitInfo.trialRemainMinutes(applicationContext)} ")
        }

        val purchaseState: Int by viewModel.billingRepository.purchaseStateFlow.collectAsStateWithLifecycle(
            lifecycle = lifecycleOwner.lifecycle,
            initialValue = Purchase.PurchaseState.UNSPECIFIED_STATE
        )

        LaunchedEffect(purchaseState) {
            if (purchaseState == Purchase.PurchaseState.PURCHASED) {
                onPremiumTreeTrialTextResource.intValue = R.string.settings_menu_your_premium_benefits
            } else if (purchaseState == Purchase.PurchaseState.PENDING) {
                onPremiumTreeTrialTextResource.intValue = R.string.settings_menu_your_premium_benefits_peding
            } else {
                onPremiumTreeTrialTextResource.intValue = R.string.settings_menu_on_premium_free_trial
            }
        }

        AutoRefreshEveryMinute {
            Column(
                modifier = Modifier.fillMaxSize()
            ) {
                // ActionBar
                Row(
                    modifier = Modifier
                        .height(58.dp)
                        .fillMaxWidth()
//                    .background(Color(0x3399ffff))
                        .padding(start = 18.dp, end = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = stringResource(id = R.string.settings_title),
                        color = contentColor,
                        style = MaterialTheme.typography.bodyLarge.copy(fontSize = 22.sp),
                        modifier = Modifier
                            .align(Alignment.CenterVertically)
                            .weight(1f)
                    )

                    // Share IconButton
                    IconButton(
                        onClick = {
//                        Toast.makeText(context, "Share clicked", Toast.LENGTH_SHORT).show()
                            val appPackageName = context.packageName
                            val appStoreLink = "https://play.google.com/store/apps/details?id=$appPackageName"
                            val appName = context.getString(R.string.app_name)
                            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                                type = "text/plain"
                                putExtra(Intent.EXTRA_TEXT, "$appName: $appStoreLink")
                            }
                            context.startActivity(Intent.createChooser(shareIntent, "Share via"))
                        },
                        modifier = Modifier.align(Alignment.CenterVertically)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Share,
                            contentDescription = "Share",
                            modifier = Modifier
                                .size(18.dp)
                                .alpha(0.75f),
                            tint = contentColor
                        )
                    }

                    // Power IconButton
                    IconButton(
                        onClick = {
                            viewModel.analyticsRepository.settingsReport(
                                dockDelay = dockingDelay.toString(),
                                haptic = dragHandleHaptic.toString(),
                                menuTransparency = (menuBarTransparency * 100).roundToInt().toString(),
                                menuComposition = menuBarConfig.name,
                                transTransparency = (translationTransparency * 100).roundToInt().toString(),
                                closeDelay = translationCloseDelay.toString(),
                                replyTransparency = (replyTransparency * 100).roundToInt().toString(),
                                correctionKit = if (useCorrectionKit) correctionKit.name else "none",
                                autoTTS = automaticTranslationPlayback.toString(),
                                TTSVoice = ttsCurrentVoice?.name ?: "unknown",
                                TTSRate = BigDecimal(ttsSpeechRate.toDouble()).setScale(1, RoundingMode.HALF_UP).toString(),
                            )

                            coroutineScope.launch {
                                delay(200L)
                                finish()
//                            moveTaskToBack(true)
                                delay(200L)
                                applicationContext.finishService()
                            }
                        },
                        modifier = Modifier.align(Alignment.CenterVertically)
                    ) {
                        Icon(
                            imageVector = Icons.Default.PowerSettingsNew,
                            contentDescription = "Exit App",
                            modifier = Modifier
                                .size(21.dp)
                                .alpha(0.75f),
                            tint = contentColor
                        )
                    }
                }

                // MenuBarView Area
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(if (isPortrait) 56.dp else 38.dp)
//                    .background(Color(0x5517fa23))
                        .onGloballyPositioned { layoutCoordinates ->
                            val center = layoutCoordinates.boundsInRoot().center
                            Timber
                                .tag(TAG)
                                .d("paddingValues $paddingValues")
                            val endPadding = paddingValues
                                .calculateRightPadding(layoutDirection)
                                .toPx(context)
                            Timber
                                .tag(TAG)
                                .d("startPadding $startPadding")
                            Timber
                                .tag(TAG)
                                .d("endPadding $endPadding")
                            val posX = (endPadding - startPadding) / 2
                            Timber
                                .tag(TAG)
                                .d("posX $posX")

                            val topPadding = paddingValues
                                .calculateTopPadding()
                                .toPx(context)
                            Timber
                                .tag(TAG)
                                .d("topPadding $topPadding")
                            val posY = center.y.toInt() - topPadding - (if (isPortrait) 0.dp else 12.dp).toPx(context)
                            Timber
                                .tag(TAG)
                                .d("menuBarViewSettlePosition ${Point(posX, posY)}")

                            menuBarViewSettlePositionFlow.value = Point(posX, posY)
                        }
                )

                Box(
                    modifier = Modifier.padding(start = contentPadding, top = contentPadding, end = contentPadding)
                ) {
                    Surface(
                        modifier = Modifier.fillMaxSize(),
                        shape = RoundedCornerShape(cornerRound),
                        color = Color.Transparent
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .verticalScroll(viewModel.scrollState)
                        ) {
                            MenuCategory(
                                painter = painterResource(id = R.drawable.ic_drag_handle),
                                categoryName = getString(R.string.settings_menu_cat_pointer),
                                isRtl = isRtl,
                            )

                            MenuItem(
                                menuItemPosition = MenuItemPosition.Top,
                                onClick = {
                                    coroutineScope.launch {
                                        settingStringFlow.value = getSecondValueText(dockingDelay)
                                        SliderDialogView.INSTANCE.cast(
                                            applicationContext = applicationContext,
                                            initialValue = dockingDelay.toFloat(),
                                            valueRange = 1000f..15000f,
                                            steps = 13,
                                            onValueChange = { value ->
                                                val posX = offset.x.toInt() + layoutCoordinates.size.width - startPadding
                                                menuBarTransparencySubtextOffset.value = Point(posX, offset.y.toInt())
                                            },
                                        contentAlignment = Alignment.CenterEnd
                                    ) {
                                        if ((1.0f - menuBarTransparency) > 0.49f) {
                                            Icon(
                                                imageVector = Icons.Default.VisibilityOff,
                                                contentDescription = "Menubar transparency",
                                                modifier = Modifier
                                                    .size(32.dp)
                                                    .padding(end = 8.dp),
                                                tint = subContentColor
                                            )
                                        } else {
                                            Text(
                                                modifier = Modifier.padding(end = 6.dp),
                                                text = getTransparencyValueText(menuBarTransparency),
                                                color = subContentColor,
                                                style = MaterialTheme.typography.bodyMedium.copy(fontSize = fontDimensionResource(R.dimen.settings_menu_subtext_size)),
                                            )
                                        }
                                    }
                                }
                            }

                            AnimatedVisibility(
                                visible = menuBarVisibility,
                                enter = expandVertically(animationSpec = tween(menuExpandAnimationDuration)),
                                exit = shrinkVertically(animationSpec = tween(menuExpandAnimationDuration)),
                                content = {
                                    fun onClick() {
                                        coroutineScope.launch {
                                            settingStringFlow.value = menuBarConfig.name
                                            SliderDialogView.INSTANCE.cast(
                                                applicationContext = applicationContext,
                                                initialValue = when (menuBarConfig) {
                                                    MenuConfig.WHOLE -> 0.0f
                                                    MenuConfig.DETECT_MODE_LANGUAGE -> 1.0f
                                                    MenuConfig.LANGUAGE_TRANSLATION_KIT -> 2.0f
                                                    MenuConfig.LANGUAGE -> 3.0f
                                                    MenuConfig.WHOLE_SHORT -> 4.0f
                                                    MenuConfig.DETECT_MODE_LANGUAGE_SHORT -> 5.0f
                                                    MenuConfig.LANGUAGE_SHORT_TRANSLATION_KIT -> 6.0f
                                                    MenuConfig.LANGUAGE_SHORT -> 7.0f
                                                    MenuConfig.DETECT_MODE_TRANSLATION_KIT -> 8.0f
                                                    MenuConfig.DETECT_MODE -> 9.0f
                                                    MenuConfig.TRANSLATION_KIT -> 10.0f

                                                    MenuConfig.V_DETECT_MODE -> 11.0f
                                                    MenuConfig.V_DETECT_MODE_TRANSLATION_KIT -> 12.0f
                                                    MenuConfig.V_LANGUAGE -> 13.0f
                                                    MenuConfig.V_LANGUAGE_TRANSLATION_KIT -> 14.0f
                                                    MenuConfig.V_DETECT_MODE_LANGUAGE -> 15.0f
                                                    MenuConfig.V_WHOLE -> 16.0f
                                                },
                                                valueRange = 0.0f..16.0f,
                                                steps = 15,
                                                onValueChange = { value ->
                                                    Timber.tag(TAG).d("onValueChange $value")
                                                    val updatedMenuBarConfig =
                                                        when (value.roundToInt().toFloat()) {
                                                            0.0f -> MenuConfig.WHOLE
                                                            1.0f -> MenuConfig.DETECT_MODE_LANGUAGE
                                                            2.0f -> MenuConfig.LANGUAGE_TRANSLATION_KIT
                                                            3.0f -> MenuConfig.LANGUAGE
                                                            4.0f -> MenuConfig.WHOLE_SHORT
                                                            5.0f -> MenuConfig.DETECT_MODE_LANGUAGE_SHORT
                                                            6.0f -> MenuConfig.LANGUAGE_SHORT_TRANSLATION_KIT
                                                            7.0f -> MenuConfig.LANGUAGE_SHORT
                                                            8.0f -> MenuConfig.DETECT_MODE_TRANSLATION_KIT
                                                            9.0f -> MenuConfig.DETECT_MODE
                                                            10.0f -> MenuConfig.TRANSLATION_KIT

                                                            11.0f -> MenuConfig.V_DETECT_MODE
                                                            12.0f -> MenuConfig.V_DETECT_MODE_TRANSLATION_KIT
                                                            13.0f -> MenuConfig.V_LANGUAGE
                                                            14.0f -> MenuConfig.V_LANGUAGE_TRANSLATION_KIT
                                                            15.0f -> MenuConfig.V_DETECT_MODE_LANGUAGE
                                                            16.0f -> MenuConfig.V_WHOLE

                                                            else -> MenuConfig.WHOLE
                                                        }
                                                    viewModel.updateMenuBarConfig(updatedMenuBarConfig)
                                                    settingStringFlow.value = updatedMenuBarConfig.name
                                                },
                                                menuText = Pair(getString(R.string.settings_menu_menubar_composition), menuBarConfigTextOffset.value),
                                                menuBarConfigText = Pair(settingStringFlow, menuBarConfigSubOffset.value),
                                                onDismissRequest = {
                                                    SliderDialogView.INSTANCE.clear()
                                                },
                                            )
                                        }
                                    }

                                    Box(
                                        modifier = Modifier
                                            .wrapContentSize()
                                            .background(
                                                color = if (isDarkMode) Color(0xFF171717) else Color(0xFFfafafa),
                                                shape = RoundedCornerShape(bottomStart = cornerRound, bottomEnd = cornerRound)
                                            )
                                    ) {
                                        HorizontalDivider(
                                            modifier = Modifier.padding(horizontal = 18.dp),
                                            thickness = 0.7.dp,
                                            color = dividerColor
                                        )

                                        Button(
                                            onClick = { onClick() },
                                            colors = ButtonDefaults.textButtonColors(contentColor = buttonColor),
                                            shape = RoundedCornerShape(bottomStart = cornerRound, bottomEnd = cornerRound),
                                            modifier = Modifier.wrapContentSize()
                                        ) {
                                            Box(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .heightIn(min = 60.dp)
                                                    .onGloballyPositioned { layoutCoordinates ->
                                                        val offset = layoutCoordinates.positionOnScreen()
                                                        val startPadding = paddingValues
                                                            .calculateLeftPadding(layoutDirection)
                                                            .toPx(context)
                                                        val posX = offset.x.toInt() + layoutCoordinates.size.width - startPadding
                                                        menuBarConfigSubOffset.value = Point(posX, offset.y.toInt())
                                                    },
                                                contentAlignment = Alignment.Center
                                            ) {
                                                MenuText(
                                                    text = getString(R.string.settings_menu_menubar_composition),
                                                    onTextPositioned = { offset ->
                                                        menuBarConfigTextOffset.value = Point(offset.x - startPadding, offset.y)
                                                    },
                                                    modifier = Modifier
                                                        .align(Alignment.TopStart)
                                                        .padding(top = 14.dp, start = 5.dp)
                                                )
                                                val scaleFactor = 0.48f
                                                Box(
                                                    modifier = Modifier
                                                        .align(Alignment.TopEnd)
                                                        .padding(top = 15.dp, bottom = 15.dp, end = 8.dp)
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
                                                    MenuBar(
                                                        menuConfig = menuBarConfig,
                                                        scaleFactor = scaleFactor,
                                                        shadowPadding = 0.dp,
                                                        borderWidth = 1.2.dp,
                                                        textDetectMode = textDetectMode,
                                                        sourceLanguageCode = sourceLanguageCode,
                                                        sourceLanguage = sourceLanguage,
                                                        targetLanguageCode = targetLanguageCode,
                                                        targetLanguage = targetLanguage,
                                                        translationKitType = kitType,
                                                        isSwappable = { sourceLanguageCode, targetLanguageCode, kitType ->
                                                            viewModel.isLanguageSwappable(sourceLanguageCode, targetLanguageCode, kitType)
                                                        },
                                                        modifier = Modifier.semantics {
                                                            contentDescription = "Menu composition"
                                                        }
                                                    )
                                                    // MenuBar 위치 클릭시 동작
                                                    Box(
                                                        modifier = Modifier
                                                            .matchParentSize()
                                                            .clickable(
                                                                onClick = { onClick() },
                                                                indication = null,  // 누름 효과 없애기
                                                                interactionSource = remember { MutableInteractionSource() }
                                                            )
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                            )

                            MenuCategory(
                                painter = painterResource(id = R.drawable.ic_ai),
                                categoryName = getString(R.string.settings_menu_cat_ai),
                                iconSize = 25.dp,
                                isRtl = isRtl,
                            )

                            MenuItem(
                                menuItemPosition = MenuItemPosition.Single,
                                onClick = {
                                    viewModel.updateUseCorrectionKit(!useCorrectionKit)
                                }
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    MenuText(
                                        text = "ChatGPT",
                                    )
                                    Switch(
                                        checked = useCorrectionKit,
                                        onCheckedChange = { value ->
                                            viewModel.updateUseCorrectionKit(value)
                                        },
                                        colors = SwitchDefaults.colors(
                                            checkedThumbColor = switchThumbColor,
                                            checkedTrackColor = switchTrackColor
                                        ),
                                        modifier = Modifier
                                            .scale(switchScale)
                                            .align(Alignment.CenterVertically)
                                            .semantics {
                                                contentDescription = if (useCorrectionKit) {
                                                    "ChatGPT text correction on"
                                                } else {
                                                    "ChatGPT text correction off"
                                                }
                                            },
                                    )
                                }
                            }

                            MenuCategory(
                                painter = painterResource(id = R.drawable.ic_translation_window),
                                categoryName = getString(R.string.settings_menu_cat_translation),
                                isRtl = isRtl,
                            )

                            MenuTextItem(
                                menuItemPosition = MenuItemPosition.Top,
                                text = getString(R.string.settings_menu_translation_transparency),
                                paddingValues = paddingValues,
                                onTextPositioned = { offset ->
                                    translationTransparencyTextOffset.value = Point(offset.x - startPadding, offset.y)
                                },
                                onGloballyPositioned = { layoutCoordinates ->
                                    val center = layoutCoordinates.boundsInWindow().center
                                    val startPadding = paddingValues.calculateLeftPadding(layoutDirection).toPx(context)
                                    val posX = center.x.toInt() - startPadding
                                    translationPoint.value = Point(posX, center.y.toInt())
                                },
                                subText = getTransparencyValueText(translationTransparency),
                                onSubtextPositioned = { offset ->
                                    translationTransparencySubtextOffset.value = Point(offset.x - startPadding, offset.y)
                                },
                                onClick = {
                                    Timber.tag(TAG).d("onClick Translation transparency ${CaptureRepository.mediaProjectionToken}")
                                    if (CaptureRepository.mediaProjectionToken == null) {
                                        // 화면 캡처 권한을 요청
                                        val intent = Intent(
                                            context,
                                            ScreenCapturePermissionRequesterActivity::class.java
                                        )
                                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                        context.startActivity(intent)
                                    } else {
                                        runTranslation(translationPoint.value, textDetectMode)
                                        coroutineScope.launch {
                                            settingStringFlow.value = getTransparencyValueText(translationTransparency)
                                            SliderDialogView.INSTANCE.cast(
                                                applicationContext = applicationContext,
                                                initialValue = 1.0f - translationTransparency,
                                                valueRange = 0.0f..0.5f,
                                                onValueChange = { value ->
                                                    viewModel.updateTranslationTransparency(1.0f - value)
                                                    settingStringFlow.value = getTransparencyValueText(1.0f - value)
                                                },
                                                menuText = Pair(getString(R.string.settings_menu_translation_transparency), translationTransparencyTextOffset.value),
                                                menuSubtext = Pair(settingStringFlow, translationTransparencySubtextOffset.value),
                                                onDismissRequest = {
                                                    closeTranslation()
                                                    SliderDialogView.INSTANCE.clear()
                                                },
                                            )
                                        }
                                    }
                                }
                            )

                            MenuTextItem(
                                menuItemPosition = MenuItemPosition.Middle,
                                text = getString(R.string.settings_menu_translation_close_delay),
                                paddingValues = paddingValues,
                                onTextPositioned = { offset ->
                                    translationCloseDelayTextOffset.value = Point(offset.x - startPadding, offset.y)
                                },
                                subText = getSecondValueText(translationCloseDelay),
                                onSubtextPositioned = { offset ->
                                    translationCloseDelaySubtextOffset.value = Point(offset.x - startPadding, offset.y)
                                },
                                onClick = {
                                    coroutineScope.launch {
                                        settingStringFlow.value = getSecondValueText(translationCloseDelay)
                                        SliderDialogView.INSTANCE.cast(
                                            applicationContext = applicationContext,
                                            initialValue = translationCloseDelay.toFloat(),
                                            valueRange = 500.0f..7000.0f,
                                            steps = 12,
                                            onValueChange = { value ->
                                                viewModel.updateTranslationCloseDelay(value.toLong())
                                                settingStringFlow.value = getSecondValueText(value.toLong())
                                            },
                                            menuText = Pair(getString(R.string.settings_menu_translation_close_delay), translationCloseDelayTextOffset.value),
                                            menuSubtext = Pair(settingStringFlow, translationCloseDelaySubtextOffset.value),
                                            onDismissRequest = {
                                                SliderDialogView.INSTANCE.clear()
                                            },
                                        )
                                    }
                                }
                            )

                            MenuItem(
                                menuItemPosition = MenuItemPosition.Middle,
                                onClick = {
                                    viewModel.updateAutomaticTranslationPlayback(!automaticTranslationPlayback)
                                }
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    MenuText(
                                        text = getString(R.string.settings_menu_automated_read_aloud),
                                    )
                                    Switch(
                                        checked = automaticTranslationPlayback,
                                        onCheckedChange = { value ->
                                            viewModel.updateAutomaticTranslationPlayback(value)
                                        },
                                        colors = SwitchDefaults.colors(
                                            checkedThumbColor = switchThumbColor,
                                            checkedTrackColor = switchTrackColor
                                        ),
                                        modifier = Modifier
                                            .scale(switchScale)
                                            .align(Alignment.CenterVertically)
                                            .semantics {
                                                contentDescription = if (automaticTranslationPlayback) {
                                                    "Automated read aloud on"
                                                } else {
                                                    "Automated read aloud off"
                                                }
                                            },
                                    )
                                }
                            }

                            MenuTextItem(
                                menuItemPosition = MenuItemPosition.Bottom,
                                text = getString(R.string.settings_menu_reply_transparency),
                                paddingValues = paddingValues,
                                onTextPositioned = { offset ->
                                    replyTransparencyTextOffset.value = Point(offset.x - startPadding, offset.y)
                                },
                                subText = getTransparencyValueText(replyTransparency),
                                onSubtextPositioned = { offset ->
                                    replyTransparencySubtextOffset.value = Point(offset.x - startPadding, offset.y)
                                },
                                onClick = {
                                    coroutineScope.launch {
                                        settingStringFlow.value = getTransparencyValueText(replyTransparency)
                                        SliderDialogView.INSTANCE.cast(
                                            applicationContext = applicationContext,
                                            initialValue = 1.0f - replyTransparency,
                                            valueRange = 0.0f..0.5f,
                                            onValueChange = { value ->
                                                viewModel.updateReplyTransparency(1.0f - value)
                                                settingStringFlow.value = getTransparencyValueText(1.0f - value)
                                            },
                                            menuText = Pair(getString(R.string.settings_menu_reply_transparency), replyTransparencyTextOffset.value),
                                            menuSubtext = Pair(settingStringFlow, replyTransparencySubtextOffset.value),
                                            onDismissRequest = {
                                                SliderDialogView.INSTANCE.clear()
                                            },
                                        )
                                    }
                                }
                            )

                            if (ttsCurrentVoice != null) {
                                MenuCategory(
                                    icon = Icons.Default.VoiceChat,
                                    categoryName = getString(R.string.settings_menu_cat_tts),
                                    isRtl = isRtl,
                                )

                                if (ttsAvailableVoices.isNotEmpty()) {
                                    MenuTextItem(
                                        menuItemPosition = MenuItemPosition.Top,
                                        text = getString(R.string.settings_menu_tts_voices),
                                        paddingValues = paddingValues,
                                        subText = ttsCurrentVoice?.name,
                                        onClick = {
                                            coroutineScope.launch {
                                                VoiceListView.INSTANCE.cast(applicationContext)
                                            }
                                        }
                                    )
                                }

                                MenuItem(
                                    menuItemPosition = if (ttsAvailableVoices.isEmpty()) MenuItemPosition.Single else MenuItemPosition.Bottom,
                                    onClick = {
                                        coroutineScope.launch {
                                            settingFloatFlow.value = ttsSpeechRate
                                            SliderDialogView.INSTANCE.cast(
                                                applicationContext = applicationContext,
                                                initialValue = ttsSpeechRate,
                                                valueRange = 0.5f..2.0f,
                                                steps = 6,
                                                onValueChange = { value ->
                                                    viewModel.updateTtsSpeechRate(value)
                                                    settingFloatFlow.value = value
                                                },
                                                menuText = Pair(getString(R.string.settings_menu_tts_rate), ttsSpeechRateTextOffset.value),
                                                speechRateText = Pair(settingFloatFlow, ttsSpeechRateIconOffset.value),
                                                onDismissRequest = {
                                                    SliderDialogView.INSTANCE.clear()
                                                },
                                            )
                                        }
                                    }
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .heightIn(min = 50.dp)
                                            .padding(end = 6.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        MenuText(
                                            text = getString(R.string.settings_menu_tts_rate),
                                            onTextPositioned = { offset ->
                                                ttsSpeechRateTextOffset.value = Point(offset.x - startPadding, offset.y)
                                            },
                                        )

                                        /*
                                            Speech rate. 1.0 is the normal speech rate,
                                            lower values slow down the speech (0.5 is half the normal speech rate),
                                            greater values accelerate it (2.0 is twice the normal speech rate).
                                         */
                                        var isToggled by remember { mutableStateOf(false) }
                                        LaunchedEffect(ttsSpeechRate) {
                                            while (true) {
                                                delay((((2.2f - ttsSpeechRate) / 4) * 1000).toLong())
                                                isToggled = !isToggled
                                            }
                                        }

                                        Crossfade(targetState = isToggled, label = "Crossfade") { toggled ->
                                            val imageResource = if (toggled) R.drawable.tts_rate_0 else R.drawable.tts_rate_1
                                            Image(
                                                painter = painterResource(id = imageResource),
                                                contentDescription = getString(R.string.settings_menu_tts_rate),
                                                colorFilter = ColorFilter.tint(Color(0xFF848487)),
                                                modifier = Modifier
                                                    .size(28.dp)
                                                    .onGloballyPositioned { layoutCoordinates ->
                                                        val offset = layoutCoordinates.positionOnScreen()
                                                        val startPadding = paddingValues
                                                            .calculateLeftPadding(layoutDirection)
                                                            .toPx(context)
                                                        val posX = offset.x.toInt() - startPadding
                                                        ttsSpeechRateIconOffset.value = Point(posX, offset.y.toInt())
                                                    }
                                            )
                                        }
                                    }
                                }
                            }

                            MenuCategory(
                                icon = Icons.Outlined.Info,
                                categoryName = getString(R.string.settings_menu_cat_about),
                                isRtl = isRtl,
                            )

                            with(sharedTransitionScope) {
                                MenuItem(
                                    menuItemPosition = MenuItemPosition.Top,
                                    onClick = {
                                        onShowPremium()
                                        viewModel.analyticsRepository.screenViewReport("Premium")
                                    },
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .heightIn(min = 50.dp)
                                            .padding(end = 6.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Column {
                                            MenuText(
                                                text = stringResource(id = onPremiumTreeTrialTextResource.intValue),
                                                modifier = Modifier
                                                    .sharedElement(
                                                        rememberSharedContentState(key = "menu_premium"),
                                                        animatedVisibilityScope = animatedVisibilityScope
                                                    )
                                            )
                                            MenuSubText(
                                                text = context.getString(R.string.settings_menu_premium_free_trial_remain, TrialLimitInfo.trialRemainMinutes(context)),
                                                paddingValues = paddingValues
                                            )
                                        }

                                        Image(
                                            painter = painterResource(id = R.drawable.image_premium_gray),
                                            contentDescription = "image_premium",
                                            colorFilter = ColorFilter.tint(Color(0xFF848487)),
                                            modifier = Modifier.size(28.dp)
                                        )
                                    }
                                }
                            }

                            MenuItem(
                                menuItemPosition = MenuItemPosition.Bottom,
                                onClick = {
                                    context.gotoStore(
                                        newTask = false,
                                        finishService = false
                                    )
                                    viewModel.analyticsRepository.screenViewReport("AppVersion")
                                },
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .heightIn(min = 50.dp)
                                        .padding(end = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    MenuText(
                                        text = getString(R.string.settings_menu_app_version),
                                    )
                                    if (latestVersionCode > versionCode) {
                                        Row(
                                            modifier = Modifier.wrapContentSize(),
                                            verticalAlignment = Alignment.CenterVertically,
                                        ) {
                                            MenuSubText(
                                                text = "${packageInfo.versionName}",
                                                paddingValues = paddingValues
                                            )
                                            Spacer(modifier = Modifier.height(4.dp))
                                            Icon(
                                                imageVector = Icons.Default.FiberNew,
                                                contentDescription = "new version",
                                                modifier = Modifier
                                                    .size(32.dp)
                                                    .padding(end = 8.dp),
                                                tint = Color(0xFF446987)
                                            )
                                        }
                                    } else {
                                        MenuSubText(
                                            text = "${packageInfo.versionName}  ${getString(R.string.settings_menu_app_version_latest)}",
                                            paddingValues = paddingValues
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(16.dp))
                        }
                    }
                }
            }
        }
    }

    @Composable
    fun MenuCategory(
        icon: ImageVector? = null,
        painter: Painter? = null,
        iconSize: Dp = 22.dp,
        categoryName: String,
        isRtl: Boolean,
    ) {
        val isDarkMode = isSystemInDarkTheme()
        val menuCategoryColor = if (isDarkMode) Color(0xFFb7b7ba) else Color(0xFF626265)

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
                .padding(start = 18.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (icon != null) {
                Icon(
                    imageVector = icon,
                    contentDescription = "$categoryName Icon",
                    modifier = Modifier
                        .size(iconSize)
                        .graphicsLayer {
                            if (isRtl) rotationY = 180f
                        },
                    tint = menuCategoryColor
                )
            }
            if (painter != null) {
                Image(
                    modifier = Modifier
                        .size(iconSize)
                        .graphicsLayer {
                            if (isRtl) rotationY = 180f
                        },
                    painter = painter,
                    contentDescription = "$categoryName Image",
                    contentScale = ContentScale.Fit,
                    colorFilter = ColorFilter.tint(menuCategoryColor)
                )
            }
            Text(
                modifier = Modifier.padding(start = 8.dp),
                text = categoryName,
                color = menuCategoryColor,
                style = MaterialTheme.typography.bodySmall.copy(fontSize = 14.sp, fontWeight = FontWeight.Bold),
            )
        }
    }

    @Composable
    fun MenuItem(
        menuItemPosition: MenuItemPosition,
        onClick: (() -> Unit)? = null,
        composable: @Composable () -> Unit,
    ) {
        val cornerRound = 32.dp
        val coroutineScope = rememberCoroutineScope()
        val shape = when (menuItemPosition) {
            MenuItemPosition.Single -> RoundedCornerShape(cornerRound)
            MenuItemPosition.Top -> RoundedCornerShape(topStart = cornerRound, topEnd = cornerRound)
            MenuItemPosition.Middle -> RoundedCornerShape(0.dp)
            MenuItemPosition.Bottom -> RoundedCornerShape(bottomStart = cornerRound, bottomEnd = cornerRound)
        }

        val isDarkMode = isSystemInDarkTheme()
        val backgroundColor = if (isDarkMode) Color(0xFF171717) else Color(0xFFfafafa)
        val dividerColor = if (isDarkMode) Color(0xFF343434) else Color(0xFFd5d5d5)
        val buttonColor = if (isDarkMode) Color(0xFFfafafa) else Color(0xFF171717)

        Box(
            modifier = Modifier
                .wrapContentSize()
                .background(
                    color = backgroundColor,
                    shape = shape
                )
        ) {
            if (menuItemPosition == MenuItemPosition.Middle || menuItemPosition == MenuItemPosition.Bottom) {
                HorizontalDivider(
                    modifier = Modifier.padding(horizontal = 18.dp),
                    thickness = 0.7.dp,
                    color = dividerColor
                )
            }

            Button(
                enabled = onClick != null,
                onClick = {
                    onClick?.let {
                        coroutineScope.launch {
                            delay(200L)
                            onClick()
                        }
                    }
                },
                colors = ButtonDefaults.textButtonColors(contentColor = buttonColor),
                shape = shape,
                modifier = Modifier
                    .wrapContentSize(),
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 50.dp)
                        .padding(top = 5.dp, bottom = 5.dp, start = 5.dp, end = 0.dp),
                ) {
                    composable()
                }
            }
        }
    }

    @Composable
    fun MenuTextItem(
        menuItemPosition: MenuItemPosition,
        text: String,
        paddingValues: PaddingValues,
        onTextPositioned: ((Point) -> Unit)? = null,
        onGloballyPositioned: ((LayoutCoordinates) -> Unit)? = null,
        subText: String? = null,
        onSubtextPositioned: ((Point) -> Unit)? = null,
        onClick: (() -> Unit)? = null,
        menuTextModifier: Modifier = Modifier
    ) {
        MenuItem(
            menuItemPosition = menuItemPosition,
            onClick = onClick,
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 50.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                MenuText(
                    text = text,
                    onGloballyPositioned = onGloballyPositioned,
                    onTextPositioned = onTextPositioned,
                    modifier = menuTextModifier
                )
                subText?.let {
                    MenuSubText(
                        text = it,
                        paddingValues = paddingValues,
                        onSubtextPositioned = onSubtextPositioned
                    )
                }
            }
        }
    }

    @Composable
    fun MenuText(
        modifier: Modifier = Modifier,
        text: String,
        onGloballyPositioned: ((LayoutCoordinates) -> Unit)? = null,
        onTextPositioned: ((Point) -> Unit)? = null,
    ) {
        val isDarkMode = isSystemInDarkTheme()
        val textColor = if (isDarkMode) Color(0xFFfcfcfc) else Color(0xFF010000)
        val fontSize = fontDimensionResource(R.dimen.settings_menu_text_size)

        Text(
            text = text,
            color = textColor,
            style = MaterialTheme.typography.bodyLarge.copy(fontSize = fontSize),
            modifier = modifier.onGloballyPositioned { layoutCoordinates ->
                onGloballyPositioned?.let { it(layoutCoordinates) }
                val offset = layoutCoordinates.positionOnScreen()
                onTextPositioned?.let { it(Point(offset.x.toInt(), offset.y.toInt())) }
            },
        )
    }

    @Composable
    fun MenuSubText(
        text: String,
        paddingValues: PaddingValues,
        onSubtextPositioned: ((Point) -> Unit)? = null,
    ) {
        val context = LocalContext.current
        val isDarkMode = isSystemInDarkTheme()
        val layoutDirection = LocalLayoutDirection.current
        val subTextColor = if (isDarkMode) Color(0xFFb7b7ba) else Color(0xFF626265)
        val fontSize = fontDimensionResource(R.dimen.settings_menu_subtext_size)

        Text(
            modifier = Modifier
                .padding(end = 6.dp)
                .onGloballyPositioned { layoutCoordinates ->
                    val offset = layoutCoordinates.positionOnScreen()
                    val startPadding = paddingValues
                        .calculateLeftPadding(layoutDirection)
                        .toPx(context)
                    val posX = offset.x.toInt() + layoutCoordinates.size.width - startPadding
                    onSubtextPositioned?.let { it(Point(offset.x.toInt() + layoutCoordinates.size.width, offset.y.toInt())) }
                },
            text = text,
            color = subTextColor,
            style = MaterialTheme.typography.bodyMedium.copy(fontSize = fontSize),
        )

    }

    private fun appReview() {
        Timber.tag(TAG).d("appReview()")
        val manager =
            if (BuildConfig.DEBUG) {
                FakeReviewManager(applicationContext)
            } else {
                ReviewManagerFactory.create(applicationContext)
            }

        lifecycleScope.launch {
            val trialCount = viewModel.secureRepository.getTrialCount()
            Timber.tag(TAG).d("appReview() trialCount $trialCount")
            val isReviewDone = viewModel.preferenceRepository.isReviewDoneFlow.first()
            Timber.tag(TAG).d("appReview() isReviewDone $isReviewDone")
            if (trialCount > 30 && !isReviewDone) {
                while (true) {
                    delay(3000L)
                    if (
                        !LanguageListView.INSTANCE.isRunning.get()
                        && !HelpTextDetectModeView.INSTANCE.isRunning.get()
                        && !HelpTranslationKitView.INSTANCE.isRunning.get()
                        && !SliderDialogView.INSTANCE.isRunning.get()
                        && !VoiceListView.INSTANCE.isRunning.get()
                    ) {
                        Timber.tag(TAG).d("All states are false. Proceeding with review flow.")
                        startReviewFlow(manager)  // 리뷰 플로우 시작
                        break  // 반복 종료
                    }
                }
            }
        }
    }

    private fun startReviewFlow(manager: ReviewManager) {
        MenuBarView.INSTANCE.clear()
        TargetHandleView.INSTANCE.clear()

        val request = manager.requestReviewFlow()
//        Timber.tag(TAG).d("appReview() startReviewFlow request $request")
        request.addOnCompleteListener { task ->
//            Timber.tag(TAG).d("appReview() startReviewFlow task ${task.isSuccessful}")
            if (task.isSuccessful) {
                val reviewInfo = task.result
                val flow = manager.launchReviewFlow(this, reviewInfo)
                flow.addOnCompleteListener { _ ->
                    viewModel.updateIsReviewDone()
                }
            } else {
                val reviewErrorCode = (task.exception as ReviewException).errorCode
                Timber.tag(TAG).d("appReview() startReviewFlow reviewErrorCode $reviewErrorCode")
            }
            lifecycleScope.launch {
                MenuBarView.INSTANCE.cast(applicationContext)
                TargetHandleView.INSTANCE.cast(applicationContext)
            }
        }
    }
}