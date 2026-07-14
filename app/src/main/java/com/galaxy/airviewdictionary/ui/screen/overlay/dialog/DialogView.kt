package com.galaxy.airviewdictionary.ui.screen.overlay.dialog


import android.content.Context
import android.content.res.Configuration
import android.graphics.PixelFormat
import android.view.Gravity
import android.view.WindowManager
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.unit.dp
import com.galaxy.airviewdictionary.ui.common.MyDialog
import com.galaxy.airviewdictionary.ui.screen.overlay.OverlayView
import javax.inject.Singleton

/**
 * 다이얼로그 뷰
 */
@Singleton
class DialogView private constructor() : OverlayView() {

    companion object {
        val INSTANCE: DialogView by lazy(LazyThreadSafetyMode.SYNCHRONIZED) { DialogView() }
    }

    private val isGlobalAlerts = mutableStateOf<Boolean>(false)
    private val icon = mutableStateOf<ImageVector?>(null)
    private val painterResource = mutableStateOf<Int?>(null)
    private val dialogTitle = mutableStateOf<String?>(null)
    private val dialogText = mutableStateOf<String?>(null)
    private val onConfirmLabel = mutableStateOf<String?>(null)
    private val onDismissLabel = mutableStateOf<String?>(null)
    private val onConfirm = mutableStateOf<(() -> Unit)?>(null)
    private val onDismiss = mutableStateOf<(() -> Unit)?>(null)

    override var layoutParams: WindowManager.LayoutParams = WindowManager.LayoutParams(
        WindowManager.LayoutParams.MATCH_PARENT,
        WindowManager.LayoutParams.MATCH_PARENT,
        WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
        WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
                or WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN
                or WindowManager.LayoutParams.FLAG_DIM_BEHIND,
        PixelFormat.TRANSLUCENT
    ).apply {
        gravity = Gravity.CENTER
        windowAnimations = android.R.style.Animation_Toast
        dimAmount = 0.50f
    }

    override val composable: @Composable () -> Unit = @Composable {
        if (isAttachedToWindow() && dialogText.value != null && onConfirm.value != null) {
            val configuration = LocalConfiguration.current
            val isPortrait = configuration.orientation == Configuration.ORIENTATION_PORTRAIT
            val screenWidth = configuration.screenWidthDp.dp
            val viewWidth = if (isPortrait) screenWidth else 420.dp

            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        .width(viewWidth)
                ) {
                    MyDialog(
                        isGlobalAlerts = isGlobalAlerts.value,
                        horizontalPadding = 24.dp,
                        icon = icon.value,
                        painterResource = painterResource.value,
                        dialogTitle = dialogTitle.value,
                        dialogText = dialogText.value!!,
                        onConfirmLabel = onConfirmLabel.value,
                        onDismissLabel = onDismissLabel.value,
                        onConfirm = {
                            onConfirm.value!!()
                            clear()
                        },
                        onDismiss = onDismiss.value,
                    )
                }
            }
        }
    }

    suspend fun cast(
        applicationContext: Context,
        isGlobalAlerts: Boolean = false,
        icon: ImageVector? = null,
        painterResource: Int? = null,
        dialogTitle: String? = null,
        dialogText: String,
        onConfirmLabel: String? = null,
        onDismissLabel: String? = null,
        onConfirm: () -> Unit,
        onDismiss: (() -> Unit)? = null,
    ) {
        this.isGlobalAlerts.value = isGlobalAlerts
        this.icon.value = icon
        this.painterResource.value = painterResource
        this.dialogTitle.value = dialogTitle
        this.dialogText.value = dialogText
        this.onConfirmLabel.value = onConfirmLabel
        this.onDismissLabel.value = onDismissLabel
        this.onConfirm.value = onConfirm
        this.onDismiss.value = onDismiss
        super.cast(applicationContext)
    }
}

















