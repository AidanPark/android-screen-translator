package com.galaxy.airviewdictionary.data.remote.firebase

import android.content.Context
import com.android.billingclient.api.Purchase
import com.galaxy.airviewdictionary.extensions.getOrCreateAppInstanceId
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale


/**
 * api 키를 안전하게 공급한다.
 */
object FireDatabase {
    fun badIntegrityReport(
        verdictAppRecognition: com.galaxy.airviewdictionary.data.local.secure.VerdictAppRecognition,
        verdictDeviceRecognition: com.galaxy.airviewdictionary.data.local.secure.VerdictDeviceRecognition,
        deviceActivityLevel: com.galaxy.airviewdictionary.data.local.secure.DeviceActivityLevel,
        verdictAppLicensing: com.galaxy.airviewdictionary.data.local.secure.VerdictAppLicensing,
        verdictPlayProtect: com.galaxy.airviewdictionary.data.local.secure.VerdictPlayProtect
    ) = Unit

    fun secureReport(eventDetail: String) = Unit

    fun screenViewReport(className: String) = Unit

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
        TTSRate: String,
    ) = Unit

    fun translationReport(
        transaction: com.galaxy.airviewdictionary.data.remote.translation.Transaction,
        textDetectMode: com.galaxy.airviewdictionary.data.local.vision.TextDetectMode?,
        correctionKitType: com.galaxy.airviewdictionary.data.remote.ai.CorrectionKitType?
    ) = Unit

    fun replyReport(transaction: com.galaxy.airviewdictionary.data.remote.translation.Transaction) = Unit

    fun hoursTakenReport(trialCount: Int, hour: Int) = Unit

    fun daysTakenReport(trialCount: Int, day: Int) = Unit

    fun purchaseReport(context: Context, purchase: Purchase) = Unit

    fun formatTimestamp(timestamp: Long): String {
        val date = Date(timestamp)
        val format = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.KOREA)
        return format.format(date)
    }
}









