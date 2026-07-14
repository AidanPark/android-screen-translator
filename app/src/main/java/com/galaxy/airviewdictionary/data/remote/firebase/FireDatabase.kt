package com.galaxy.airviewdictionary.data.remote.firebase

import android.content.Context
import com.galaxy.airviewdictionary.data.local.vision.TextDetectMode
import com.galaxy.airviewdictionary.extensions.getOrCreateAppInstanceId
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.MutableData
import com.google.firebase.database.Transaction
import org.json.JSONArray
import org.json.JSONObject
import timber.log.Timber
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.roundToInt


/**
 * api 키를 안전하게 공급한다.
 */
object FireDatabase {

    private val TAG = javaClass.simpleName

    private fun sanitizeKey(input: String): String {
        val disallowedChars = "[.$#\\[\\]/]".toRegex()
        return input.replace(disallowedChars, "_")  // 특수 문자를 밑줄로 대체
    }

    fun secureReport(eventDetail: String) {
        FirebaseAuth.getInstance().signInAnonymously().addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val dbRef = FirebaseDatabase.getInstance().reference
                val userRef = dbRef.child("secureReport").child(sanitizeKey(eventDetail))

                userRef.runTransaction(object : Transaction.Handler {
                    override fun doTransaction(currentData: MutableData): Transaction.Result {
                        val currentCount = currentData.getValue(Int::class.java) ?: 0
                        currentData.value = currentCount + 1
                        return Transaction.success(currentData)
                    }

                    override fun onComplete(
                        error: DatabaseError?,
                        committed: Boolean,
                        currentData: DataSnapshot?
                    ) {
                        if (error != null) {
                            Timber.tag(TAG).e("secureReport failed: ${error.message}")
                        } else {
                            Timber.tag(TAG).d("secureReport successfully")
                        }
                    }
                })
            } else {
                Timber.tag(TAG).e("Authentication failed: ${task.exception}")
            }
        }
    }

    fun screenViewReport(className: String) {
        FirebaseAuth.getInstance().signInAnonymously().addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val dbRef = FirebaseDatabase.getInstance().reference
                val userRef = dbRef.child("screenViewReport").child(sanitizeKey(className))

                userRef.runTransaction(object : Transaction.Handler {
                    override fun doTransaction(currentData: MutableData): Transaction.Result {
                        val currentCount = currentData.getValue(Int::class.java) ?: 0
                        currentData.value = currentCount + 1
                        return Transaction.success(currentData)
                    }

                    override fun onComplete(
                        error: DatabaseError?,
                        committed: Boolean,
                        currentData: DataSnapshot?
                    ) {
                        if (error != null) {
                            Timber.tag(TAG).e("screenViewReport failed: ${error.message}")
                        } else {
                            Timber.tag(TAG).d("screenViewReport successfully")
                        }
                    }
                })
            } else {
                Timber.tag(TAG).e("Authentication failed: ${task.exception}")
            }
        }
    }

    fun settingsReport(
        dockDelay: String,
        haptic: String,
        menuTransparency: String,
        menuComposition: String,
        transTransparency: String,
        closeDelay: String,
        replyTransparency: String,
        autoTTS: String,
        TTSRate: String,
    ) {
        FirebaseAuth.getInstance().signInAnonymously().addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val dbRef = FirebaseDatabase.getInstance().reference
                val reportObject = JSONObject().apply {
                    put(Param.DOCKING_DELAY, dockDelay)
                    put(Param.DRAG_HANDLE_HAPTIC, haptic)
                    put(Param.MENU_BAR_TRANSPARENCY, menuTransparency)
                    put(Param.MENU_BAR_COMPOSITION, menuComposition)
                    put(Param.TRANSLATION_TRANSPARENCY, transTransparency)
                    put(Param.TRANSLATION_CLOSE_DELAY, closeDelay)
                    put(Param.REPLY_TRANSPARENCY, replyTransparency)
                    put(Param.AUTOMATIC_TRANSLATION_PLAYBACK, autoTTS)
                    put(Param.TTS_SPEECH_RATE, TTSRate)
                }
                val userRef = dbRef.child("settingsReport").child(sanitizeKey(reportObject.toString()))

                userRef.runTransaction(object : Transaction.Handler {
                    override fun doTransaction(currentData: MutableData): Transaction.Result {
                        val currentCount = currentData.getValue(Int::class.java) ?: 0
                        currentData.value = currentCount + 1
                        return Transaction.success(currentData)
                    }

                    override fun onComplete(
                        error: DatabaseError?,
                        committed: Boolean,
                        currentData: DataSnapshot?
                    ) {
                        if (error != null) {
                            Timber.tag(TAG).e("settingsReport failed: ${error.message}")
                        } else {
                            Timber.tag(TAG).d("settingsReport successfully")
                        }
                    }
                })
            } else {
                Timber.tag(TAG).e("Authentication failed: ${task.exception}")
            }
        }
    }

    fun translationReport(
        transaction: com.galaxy.airviewdictionary.data.remote.translation.Transaction,
        textDetectMode: TextDetectMode?,
    ) {
        FirebaseAuth.getInstance().signInAnonymously().addOnCompleteListener { task ->
            Timber.tag(TAG).d("task.isSuccessful ${task.isSuccessful}")
            if (task.isSuccessful) {
                val dbRef = FirebaseDatabase.getInstance().reference
                val reportObject = JSONObject().apply {
                    put(Param.SOURCE_LANGUAGE_CODE, transaction.sourceLanguageCode ?: "unknown")
                    put(Param.TARGET_LANGUAGE_CODE, transaction.targetLanguageCode ?: "unknown")
                    put(Param.TRANSLATION_KIT_TYPE, transaction.translationKitType?.name ?: "unknown")
                    put(Param.TEXT_DETECT_MODE, textDetectMode?.name ?: "unknown")
                    put(Param.DETECTED_LANGUAGE_CODE, transaction.detectedLanguageCode ?: "unknown")
                }
                val userRef = dbRef.child("translationReport").child(sanitizeKey(reportObject.toString()))

                userRef.runTransaction(object : Transaction.Handler {
                    override fun doTransaction(currentData: MutableData): Transaction.Result {
                        val currentCount = currentData.getValue(Int::class.java) ?: 0
                        currentData.value = currentCount + 1
                        return Transaction.success(currentData)
                    }

                    override fun onComplete(
                        error: DatabaseError?,
                        committed: Boolean,
                        currentData: DataSnapshot?
                    ) {
                        if (error != null) {
                            Timber.tag(TAG).e("translationReport failed: ${error.message}")
                        } else {
                            Timber.tag(TAG).d("translationReport successfully")
                        }
                    }
                })
            } else {
                Timber.tag(TAG).e("Authentication failed: ${task.exception}")
            }
        }
    }

    fun replyReport(transaction: com.galaxy.airviewdictionary.data.remote.translation.Transaction) {
        Timber.tag("replyReport").d("📋 replyReport 3 : $transaction")
        FirebaseAuth.getInstance().signInAnonymously().addOnCompleteListener { task ->
            Timber.tag(TAG).d("task.isSuccessful ${task.isSuccessful}")
            if (task.isSuccessful) {
                val dbRef = FirebaseDatabase.getInstance().reference
                val reportObject = JSONObject().apply {
                    put(Param.SOURCE_LANGUAGE_CODE, transaction.sourceLanguageCode ?: "unknown")
                    put(Param.TARGET_LANGUAGE_CODE, transaction.targetLanguageCode ?: "unknown")
                    put(Param.TRANSLATION_KIT_TYPE, transaction.translationKitType?.name ?: "unknown")
                }
                val userRef = dbRef.child("replyReport").child(sanitizeKey(reportObject.toString()))

                userRef.runTransaction(object : Transaction.Handler {
                    override fun doTransaction(currentData: MutableData): Transaction.Result {
                        val currentCount = currentData.getValue(Int::class.java) ?: 0
                        currentData.value = currentCount + 1
                        return Transaction.success(currentData)
                    }

                    override fun onComplete(
                        error: DatabaseError?,
                        committed: Boolean,
                        currentData: DataSnapshot?
                    ) {
                        if (error != null) {
                            Timber.tag(TAG).e("replyReport failed: ${error.message}")
                            Timber.tag("replyReport").e("replyReport failed: ${error.message}")
                        } else {
                            Timber.tag(TAG).d("replyReport successfully")
                            Timber.tag("replyReport").d("replyReport successfully")
                        }
                    }
                })
            } else {
                Timber.tag(TAG).e("Authentication failed: ${task.exception}")
            }
        }
    }

    fun hoursTakenReport(trialCount: Int, hour: Int) {
        FirebaseAuth.getInstance().signInAnonymously().addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val dbRef = FirebaseDatabase.getInstance().reference
                val reportObject = JSONObject().apply {
                    put("${Param.HOURS_TAKEN}$trialCount", hour.toLong())
                }
                val userRef = dbRef.child("hoursTakenReport").child(sanitizeKey(reportObject.toString()))

                userRef.runTransaction(object : Transaction.Handler {
                    override fun doTransaction(currentData: MutableData): Transaction.Result {
                        val currentCount = currentData.getValue(Int::class.java) ?: 0
                        currentData.value = currentCount + 1
                        return Transaction.success(currentData)
                    }

                    override fun onComplete(
                        error: DatabaseError?,
                        committed: Boolean,
                        currentData: DataSnapshot?
                    ) {
                        if (error != null) {
                            Timber.tag(TAG).e("hoursTakenReport failed: ${error.message}")
                        } else {
                            Timber.tag(TAG).d("hoursTakenReport successfully")
                        }
                    }
                })
            } else {
                Timber.tag(TAG).e("Authentication failed: ${task.exception}")
            }
        }
    }

    fun daysTakenReport(trialCount: Int, day: Int) {
        FirebaseAuth.getInstance().signInAnonymously().addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val dbRef = FirebaseDatabase.getInstance().reference
                val reportObject = JSONObject().apply {
                    put("${Param.DAYS_TAKEN}$trialCount", day.toLong())
                }
                val userRef = dbRef.child("daysTakenReport").child(sanitizeKey(reportObject.toString()))

                userRef.runTransaction(object : Transaction.Handler {
                    override fun doTransaction(currentData: MutableData): Transaction.Result {
                        val currentCount = currentData.getValue(Int::class.java) ?: 0
                        currentData.value = currentCount + 1
                        return Transaction.success(currentData)
                    }

                    override fun onComplete(
                        error: DatabaseError?,
                        committed: Boolean,
                        currentData: DataSnapshot?
                    ) {
                        if (error != null) {
                            Timber.tag(TAG).e("daysTakenReport failed: ${error.message}")
                        } else {
                            Timber.tag(TAG).d("daysTakenReport successfully")
                        }
                    }
                })
            } else {
                Timber.tag(TAG).e("Authentication failed: ${task.exception}")
            }
        }
    }

    fun formatTimestamp(timestamp: Long): String {
        val date = Date(timestamp)
        val format = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.KOREA)
        return format.format(date)
    }
}









