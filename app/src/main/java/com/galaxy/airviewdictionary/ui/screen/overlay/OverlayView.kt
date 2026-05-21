package com.galaxy.airviewdictionary.ui.screen.overlay

import android.app.Service
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.graphics.drawable.RippleDrawable
import android.os.Build
import android.os.IBinder
import android.view.View
import android.view.WindowManager
import androidx.annotation.CallSuper
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.ComposeView
import androidx.lifecycle.setViewTreeLifecycleOwner
import androidx.savedstate.setViewTreeSavedStateRegistryOwner
import com.galaxy.airviewdictionary.core.OverlayService
import com.galaxy.airviewdictionary.core.OverlayServiceEventListener
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import timber.log.Timber
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException


/**
 * 오버레이 뷰
 * [cast] 함수를 이용하여 윈도우에 view 를 부착요청 하게 된다.
 * 오버레이 서비스 [com.galaxy.airviewdictionary.core.OverlayService] 를 실행시키고 오버레이 뷰를 바인드 한다.
 */
abstract class OverlayView : OverlayServiceEventListener {

    protected val TAG = javaClass.simpleName

    open val isRunning = AtomicBoolean(false)

    private var avdCoroutineScope = CoroutineScope(Dispatchers.IO + Job())

    private var overlayViewCoroutineScope = CoroutineScope(Dispatchers.Main + Job())

    ////////////////////////////////////////////////////////////////////////////////////////////////
    //                                                                                            //
    //                                        View content                                        //
    //                                                                                            //
    ////////////////////////////////////////////////////////////////////////////////////////////////

    private lateinit var windowManager: WindowManager

    lateinit var overlayService: OverlayService

    var view: ComposeView? = null

    private var oldViewDetachRunnable: Runnable? = null

    private var oldView: View? = null

    abstract val layoutParams: WindowManager.LayoutParams

    abstract val composable: @Composable () -> Unit

    open val touchListener: (applicationContext: Context) -> View.OnTouchListener? = { _ -> null }

    @CallSuper
    open suspend fun cast(applicationContext: Context, reattach: Boolean = false) {
        Timber.tag(TAG).i("#### cast ####")
        avdCoroutineScope = CoroutineScope(Dispatchers.IO + Job())
        overlayViewCoroutineScope = CoroutineScope(Dispatchers.Main + Job())
        overlayService = getOverlayService(applicationContext)

        launchInOverlayViewCoroutineScope {
            val oldView = if (reattach && view?.isAttachedToWindow == true) view else null
            if (view == null || reattach) {
                view = ComposeView(overlayService).apply {
                    setViewTreeLifecycleOwner(overlayService)
                    setViewTreeSavedStateRegistryOwner(overlayService)
                    setContent(composable)
                    touchListener(overlayService.applicationContext)?.let {
                        setOnTouchListener(it)
                    }
                    if (reattach && oldView != null) {
                        visibility = View.INVISIBLE
                    }
                }
            }

            val localView = view
            if (localView != null && !localView.isAttachedToWindow) {
                launchInOverlayViewCoroutineScope {
                    try {
                        getWindowManager(overlayService.applicationContext).addView(localView, layoutParams)
                        if (reattach && oldView != null) {
                            localView.setLayerType(View.LAYER_TYPE_HARDWARE, null)
                            oldView.setLayerType(View.LAYER_TYPE_HARDWARE, null)

                            // 저장해두기
                            this@OverlayView.oldView = oldView
                            // 이전 Runnable이 있다면 먼저 제거
                            oldView.removeCallbacks(oldViewDetachRunnable)

                            // 새로운 Runnable 등록
                            oldViewDetachRunnable = Runnable {
                                localView.visibility = View.VISIBLE
                                oldView.visibility = View.INVISIBLE
                                oldView.post {
                                    try {
                                        val rippleDrawable = view?.background as? RippleDrawable
                                        rippleDrawable?.setVisible(false, false)
                                        rippleDrawable?.state = intArrayOf() // ripple 해제
                                        getWindowManager(overlayService.applicationContext).removeView(oldView)
                                    } catch (e: Exception) {
                                        try {
                                            getWindowManager(overlayService.applicationContext).removeViewImmediate(oldView)
                                        } catch (e: Exception) {
                                            Timber.tag(TAG).e(e, "oldView removeViewImmediate 실패")
                                        }
                                    }
                                }
                                // 작업이 완료되면 정리
                                this@OverlayView.oldView = null
                                oldViewDetachRunnable = null
                            }

                            oldView.postDelayed(oldViewDetachRunnable, 900)
                        }
                    } catch (_: IllegalStateException) {
                        // 이미 윈도우에 추가된 경우 발생할 수 있는 예외 처리
                    }
                }
            }
        }

        isRunning.set(true)
    }

    open suspend fun cast(
        applicationContext: Context,
        posX: Int,
        posY: Int,
        reattach: Boolean = false
    ) {
        layoutParams.x = posX
        layoutParams.y = posY
        this.cast(applicationContext, reattach)
    }

    open suspend fun cast(applicationContext: Context) {
        this.cast(applicationContext, false)
    }

    @CallSuper
    open fun onServiceConnected(overlayService: OverlayService) {
        overlayService.registerListener(this@OverlayView)
    }

    private fun getWindowManager(applicationContext: Context): WindowManager {
        if (!::windowManager.isInitialized) {
            windowManager = applicationContext.getSystemService(Context.WINDOW_SERVICE) as WindowManager
        }
        return windowManager
    }

    fun isAttachedToWindow(): Boolean {
        if (view != null) {
            return view!!.isAttachedToWindow
        }
        return false
    }

    fun isServiceInitialized(): Boolean {
        return ::overlayService.isInitialized
    }

    open fun updateLayout(applicationContext: Context) {
        view?.let {
            if (isAttachedToWindow()) {
                getWindowManager(applicationContext).updateViewLayout(it, layoutParams)
            }
        }
    }

    open fun updateLayout(context: Context, posX: Int, posY: Int) {
        layoutParams.x = posX
        layoutParams.y = posY
        updateLayout(context)
    }

    protected fun launchInAVDCoroutineScope(block: suspend CoroutineScope.() -> Unit): Job {
        return avdCoroutineScope.launch(block = block)
    }

    protected fun launchInOverlayViewCoroutineScope(block: suspend CoroutineScope.() -> Unit): Job {
        return overlayViewCoroutineScope.launch(block = block)
    }


    ////////////////////////////////////////////////////////////////////////////////////////////////
    //                                                                                            //
    //                                 OverlayService provider                                    //
    //                                                                                            //
    ////////////////////////////////////////////////////////////////////////////////////////////////

    private var serviceConnector: ServiceConnector<OverlayService>? = null

    private suspend fun getOverlayService(applicationContext: Context): OverlayService {
        serviceConnector = ServiceConnector(
            context = applicationContext,
            serviceClass = OverlayService::class.java,
            onConnected = { service -> onServiceConnected(service) }
        )
        return serviceConnector!!.bind()
    }


    ////////////////////////////////////////////////////////////////////////////////////////////////
    //                                                                                            //
    //                                  OverlayServiceEventListener                               //
    //                                                                                            //
    ////////////////////////////////////////////////////////////////////////////////////////////////

    @CallSuper
    override fun onOverlayServiceEvent(overlayService: OverlayService, event: Event) {
        Timber.tag(TAG).i("#### super onOverlayServiceEvent($event) ####")
        if (event == Event.Unbind) {
            clear()
        }
    }

    @CallSuper
    open fun clear() {
        overlayViewCoroutineScope.cancel()
        avdCoroutineScope.cancel()
        isRunning.set(false)

        // 지연된 oldView 정리
        oldViewDetachRunnable?.let { runnable ->
            oldView?.removeCallbacks(runnable)
            oldView?.let {
                try {
                    val rippleDrawable = view?.background as? RippleDrawable
                    rippleDrawable?.setVisible(false, false)
                    rippleDrawable?.state = intArrayOf() // ripple 해제
                    getWindowManager(overlayService.applicationContext).removeView(it)
                } catch (e: Exception) {
                    try {
                        getWindowManager(overlayService.applicationContext).removeViewImmediate(it)
                    } catch (e: Exception) {
                        Timber.tag(TAG).e("$TAG remove oldView 실패 ${oldView?.isAttachedToWindow}")
                    }
                }
            }
        }
        oldViewDetachRunnable = null
        this@OverlayView.oldView = null

        try {
            val rippleDrawable = view?.background as? RippleDrawable
            rippleDrawable?.setVisible(false, false)
            rippleDrawable?.state = intArrayOf() // ripple 해제
            getWindowManager(overlayService.applicationContext).removeView(view)
        } catch (e: Exception) {
            try {
                getWindowManager(overlayService.applicationContext).removeViewImmediate(view)
            } catch (e: Exception) {
                Timber.tag(TAG).e("$TAG removeView 실패 ${isAttachedToWindow()}")
            }
        }
        view = null

        if (::overlayService.isInitialized) {
            overlayService.unregisterListener(this@OverlayView)
        }

        serviceConnector?.unbind()
        serviceConnector = null
    }
}


class ServiceConnector<T : Service>(
    private val context: Context,
    private val serviceClass: Class<T>,
    private val onConnected: (T) -> Unit
) {
    private val TAG = "ServiceConnector"

    private var connection: ServiceConnection? = null
    private var isBound = false

    suspend fun bind(): T = suspendCancellableCoroutine { continuation ->
        val intent = Intent(context, serviceClass)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            context.startForegroundService(intent)
        } else {
            context.startService(intent)
        }
        connection = object : ServiceConnection {
            override fun onServiceConnected(name: ComponentName?, binder: IBinder?) {
                isBound = true
                val localBinder = binder as? OverlayService.LocalBinder
                val service = localBinder?.getService() as? T
                if (service != null) {
                    onConnected(service)
                    continuation.resume(service)
                } else {
                    continuation.resumeWithException(IllegalStateException("Failed to bind service"))
                }
            }

            override fun onServiceDisconnected(name: ComponentName?) {
                isBound = false
            }
        }

        context.bindService(intent, connection!!, Context.BIND_AUTO_CREATE)
    }

    fun unbind() {
        if (isBound && connection != null) {
            try {
                context.unbindService(connection!!)
            } catch (e: Exception) {
                Timber.tag(TAG).e("unbindService failed: $e")
            }
        }
        connection = null
        isBound = false
    }
}
