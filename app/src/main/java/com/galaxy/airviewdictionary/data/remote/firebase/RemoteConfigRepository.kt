package com.galaxy.airviewdictionary.data.remote.firebase

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject
import javax.inject.Singleton


data class RemoteConfigValue(private val rawValue: String) {
    fun asString(): String = rawValue

    fun asLong(): Long = rawValue.toLongOrNull() ?: 0L
}


@Singleton
class RemoteConfigRepository @Inject constructor() {

    companion object PreferencesKeys {
        const val SERVICE_AVAILABLE_KEY = "service_available"
        const val LATEST_VERSION_CODE_KEY = "latest_version_code"
        const val FORCE_UPDATE_VERSION_CODE_KEY = "force_update_version_code"
        const val API_KEY_VERSION_AZURE = "api_key_version_azure"
        const val API_KEY_VERSION_DEEPL = "api_key_version_deepl"
        const val API_KEY_VERSION_PAPAGO = "api_key_version_papago"
        const val API_KEY_VERSION_YANDEX = "api_key_version_yandex"
        const val API_KEY_VERSION_CHATGPT = "api_key_version_chatgpt"
        const val TRIAL_TIME_LIMIT_MINUTE = "trial_time_limit_minute"
        const val FIXED_AREA_VIEW_CAMPAIGN_PERIOD_MINUTE = "fixed_area_view_campaign_period_minute"
        const val AD_UNIT_ID = "ad_unit_id"
    }

    private val _remoteConfigFlow = MutableStateFlow<Map<String, RemoteConfigValue>>(emptyMap())

    val remoteConfigFlow: StateFlow<Map<String, RemoteConfigValue>> get() = _remoteConfigFlow
}










