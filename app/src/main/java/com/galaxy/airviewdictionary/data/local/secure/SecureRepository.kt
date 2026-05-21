package com.galaxy.airviewdictionary.data.local.secure

import android.content.Context
import com.galaxy.airviewdictionary.data.AVDRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Minimal secure repository without Play Integrity / GMS dependencies.
 */
@Singleton
class SecureRepository @Inject constructor(@ApplicationContext val context: Context) : AVDRepository() {

    companion object {
        var VERDICT_APP_RECOGNITION_FAILED = false
    }

    private var trialCount = 0

    private val _secureAssessmentInfoFlow = MutableStateFlow<SecureAssessmentInfo?>(SecureAssessmentInfo())

    val secureAssessmentInfoFlow: StateFlow<SecureAssessmentInfo?> get() = _secureAssessmentInfoFlow

    fun increaseTrialCount(): Int {
        trialCount += 1
        return trialCount
    }

    fun getTrialCount(): Int = trialCount

    fun playIntegrity(
        apiKeyVersionAzure: Int = 1,
        apiKeyVersionDeepl: Int = 1,
        apiKeyVersionPapago: Int = 1,
        apiKeyVersionYandex: Int = 1,
        apiKeyVersionChatgpt: Int = 1,
        retry: Boolean = false
    ) {
        _secureAssessmentInfoFlow.value = SecureAssessmentInfo()
    }

    private fun initApiKeyInfo() {
        _secureAssessmentInfoFlow.value = SecureAssessmentInfo()
    }

    private fun securityAssessment() {
        _secureAssessmentInfoFlow.value = SecureAssessmentInfo()
    }

    init {
        initApiKeyInfo()
    }
}
