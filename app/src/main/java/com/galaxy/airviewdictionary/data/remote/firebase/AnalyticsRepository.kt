package com.galaxy.airviewdictionary.data.remote.firebase

import android.content.Context
import com.galaxy.airviewdictionary.BuildConfig
import com.galaxy.airviewdictionary.data.remote.ai.CorrectionKitType
import com.galaxy.airviewdictionary.data.local.vision.TextDetectMode
import com.galaxy.airviewdictionary.data.remote.translation.Transaction
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton


object Event {
    const val SECURE = "secure"
    const val TRANSLATE = "translate"
    const val PURCHASE_INDUCEMENT = "purchase_inducement"
    const val TIME_TAKEN = "time_taken"
}

object Param {
    const val SECURE_DETAIL = "detail"

    const val TEXT_DETECT_MODE = "detectMode"
    const val SOURCE_LANGUAGE_CODE = "sourceCode"
    const val TARGET_LANGUAGE_CODE = "targetCode"
    const val DETECTED_LANGUAGE_CODE = "detectedCode"
    const val TRANSLATION_KIT_TYPE = "kit"

    const val DOCKING_DELAY = "dockDelay"
    const val DRAG_HANDLE_HAPTIC = "haptic"
    const val MENU_BAR_TRANSPARENCY = "menuTransparency"
    const val MENU_BAR_COMPOSITION = "menuComposition"
    const val TRANSLATION_TRANSPARENCY = "transTransparency"
    const val TRANSLATION_CLOSE_DELAY = "closeDelay"
    const val REPLY_TRANSPARENCY = "replyTransparency"
    const val CORRECTION_KIT_TYPE = "correct"
    const val AUTOMATIC_TRANSLATION_PLAYBACK = "autoTTS"
    const val TTS_VOICE = "TTSVoice"
    const val TTS_SPEECH_RATE = "TTSRate"

    const val INSTALL_COUNT = "install_count"
    const val TRIAL_COUNT = "trial_count"

    const val HOURS_TAKEN = "hours_"
    const val DAYS_TAKEN = "days_"
}

@Singleton
class AnalyticsRepository @Inject constructor(@ApplicationContext val context: Context) {

    fun secureReport(eventDetail: String) {
        if (BuildConfig.DEBUG) return
    }

    fun screenViewReport(className: String) {
        if (BuildConfig.DEBUG) return
    }

    fun settingsReport(
        dockDelay: String,
        haptic: String,
        menuTransparency: String,
        menuComposition: String,
        transTransparency: String,
        closeDelay: String,
        replyTransparency: String,
        correctionKit: String,
        autoTTS: String,
        TTSVoice: String,
        TTSRate: String,
    ) {
        if (BuildConfig.DEBUG) return
    }

    fun translationReport(
        transaction: Transaction,
        textDetectMode: TextDetectMode?,
        correctionKitType: CorrectionKitType?
    ) {
        if (BuildConfig.DEBUG) return
    }

    fun replyReport(transaction: Transaction) {
        if (BuildConfig.DEBUG) return
    }

    fun hoursTakenReport(trialCount: Int, hour: Int) {
        if (BuildConfig.DEBUG) return
    }

    fun daysTakenReport(trialCount: Int, day: Int) {
        if (BuildConfig.DEBUG) return
    }

}










