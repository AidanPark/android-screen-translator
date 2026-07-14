package com.galaxy.airviewdictionary.data.local.secure

import android.content.Context
import com.galaxy.airviewdictionary.data.AVDRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton


/**
 * 로컬 보안 저장소 접근 (무료 번역 시행 횟수 관리).
 */
@Singleton
class SecureRepository @Inject constructor(@ApplicationContext val context: Context) : AVDRepository() {

    fun increaseTrialCount(): Int {
        val trialCount = getTrialCount() + 1
        SecureStore.set(context, SecureStoreKey.TRANSLATE_TRIAL_COUNT, trialCount.toString())
        return trialCount
    }

    fun getTrialCount(): Int {
        return SecureStore.get(context, SecureStoreKey.TRANSLATE_TRIAL_COUNT)?.get()?.toInt() ?: 0
    }

    init {
        Timber.tag(TAG).i("=========================== SecureRepository ==========================")
    }

    override fun onZeroReferences() {

    }
}
