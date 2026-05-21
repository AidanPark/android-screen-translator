package com.galaxy.airviewdictionary.data.remote.translation.goolge

import com.galaxy.airviewdictionary.data.remote.translation.TranslationKit
import com.galaxy.airviewdictionary.data.remote.translation.TranslationKitType
import com.galaxy.airviewdictionary.data.remote.translation.Language
import com.galaxy.airviewdictionary.data.remote.translation.Transaction
import com.galaxy.airviewdictionary.data.remote.translation.TranslationResponse
import javax.inject.Inject
import javax.inject.Singleton


/**
 *
 */
@Singleton
class GoogleMlKit @Inject constructor() : TranslationKit() {

    override fun available(): Boolean {
        return false
    }

    fun available(sourceLanguageCode: String, targetLanguageCode: String): Boolean = false

    fun downloadLanguage(languageCode: String) = Unit

    override suspend fun request(
        sourceLanguageCode: String,
        targetLanguageCode: String,
        sourceText: String
    ): TranslationResponse {
        return TranslationResponse.Error(UnsupportedOperationException("GoogleMlKit is disabled because Google Play services dependencies were removed."))
    }

    private val availableLanguages: List<Language> = emptyList()

    override val supportedLanguagesAsSource: List<Language> = availableLanguages

    override val supportedLanguagesAsTarget: List<Language> = availableLanguages

    override fun isSupportedAsSource(code: String, targetLanguageCode: String): Boolean {
        return false
    }

    override fun isSupportedAsTarget(code: String, sourceLanguageCode: String): Boolean {
        return false
    }

    override fun isLanguageSwappable(sourceLanguageCode: String, targetLanguageCode: String): Boolean {
        return false
    }

    fun close() = Unit
}
