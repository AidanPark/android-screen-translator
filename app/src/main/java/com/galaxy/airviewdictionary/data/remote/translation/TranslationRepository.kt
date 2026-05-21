package com.galaxy.airviewdictionary.data.remote.translation

import com.galaxy.airviewdictionary.data.AVDRepository
import com.galaxy.airviewdictionary.data.remote.translation.azure.AzureKit
import com.galaxy.airviewdictionary.data.remote.translation.deepl.DeepLKit
import com.galaxy.airviewdictionary.data.remote.translation.goolge.GoogleWebKit
import com.galaxy.airviewdictionary.data.remote.translation.papago.PapagoKit
import com.galaxy.airviewdictionary.data.remote.translation.Language
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton


/**
 *
 */
@Singleton
class TranslationRepository @Inject constructor(
    private val googleWebKit: GoogleWebKit,
//    private val googleMlKit: GoogleMlKit,
    private val azureKit: AzureKit,
    private val deepLKit: DeepLKit,
//    private val yandexKit: YandexKit,
    private val papagoKit: PapagoKit
) : AVDRepository() {

    // 라틴 문자를 사용하는 언어 코드 리스트
    private val latinLanguages = setOf("en", "es", "fr", "de", "pt", "it", "ro", "nl", "sv", "no", "da", "fi", "pl", "cs", "hu", "sk", "sl")

    val supportedLanguagesAsSource: List<Language> by lazy {
        val (autoLanguages, otherLanguages) = mergeLanguages(
            googleWebKit.supportedLanguagesAsSource,
//            googleMlKit.supportedLanguagesAsSource,
            azureKit.supportedLanguagesAsSource,
            deepLKit.supportedLanguagesAsSource,
//            yandexKit.supportedLanguagesAsSource,
            papagoKit.supportedLanguagesAsSource,
        ).partition { it.code.equals("auto", ignoreCase = true) }

        val userLanguageCode = Locale.getDefault().language
        if (userLanguageCode in latinLanguages) {
            autoLanguages + otherLanguages.sorted()
        } else {
            val (noDisplayNames, regularLanguages) = otherLanguages.partition { language ->
                language.code.uppercase() in Language.noDisplayNameList
            }
            autoLanguages + regularLanguages.sorted() + noDisplayNames.sorted()
        }
    }

    val supportedLanguagesAsTarget: List<Language> by lazy {
        val mergedLanguages = mergeLanguages(
            googleWebKit.supportedLanguagesAsTarget,
//            googleMlKit.supportedLanguagesAsTarget,
            azureKit.supportedLanguagesAsTarget,
            deepLKit.supportedLanguagesAsTarget,
            azureKit.supportedLanguagesAsTarget,
//            yandexKit.supportedLanguagesAsTarget,
            papagoKit.supportedLanguagesAsTarget,
        )

        val userLanguageCode = Locale.getDefault().language
        if (userLanguageCode in latinLanguages) {
            mergedLanguages.sorted()
        } else {
            val (noDisplayNames, regularLanguages) = mergedLanguages.partition { language ->
                language.code.uppercase() in Language.noDisplayNameList
            }
            regularLanguages.sorted() + noDisplayNames.sorted()
        }
    }

    fun getSupportedLanguages(kitType: TranslationKitType): List<Language> {
        val languages = when (kitType) {
//            TranslationKitType.GOOGLE -> googleWebKit.supportedLanguagesAsSource + googleWebKit.supportedLanguagesAsTarget + googleMlKit.supportedLanguagesAsSource + googleMlKit.supportedLanguagesAsTarget
            TranslationKitType.GOOGLE -> googleWebKit.supportedLanguagesAsSource + googleWebKit.supportedLanguagesAsTarget
            TranslationKitType.AZURE -> azureKit.supportedLanguagesAsSource + azureKit.supportedLanguagesAsTarget
            TranslationKitType.DEEPL -> deepLKit.supportedLanguagesAsSource + deepLKit.supportedLanguagesAsTarget
//            TranslationKitType.YANDEX -> yandexKit.supportedLanguagesAsSource + yandexKit.supportedLanguagesAsTarget
            TranslationKitType.PAPAGO -> papagoKit.supportedLanguagesAsSource + papagoKit.supportedLanguagesAsTarget
        }
        return languages
            .distinctBy { it.code.uppercase() }
            .sortedBy { it.displayName }
    }

    fun getSupportedSourceLanguage(code: String): Language {
        return supportedLanguagesAsSource.find { it.code.equals(code, ignoreCase = true) } ?: Language(code)
    }

    fun getSupportedTargetLanguage(code: String): Language {
        return supportedLanguagesAsTarget.find { it.code.equals(code, ignoreCase = true) } ?: Language(code)
    }

    private fun mergeLanguages(vararg lists: List<Language>): List<Language> {
        // Combine all lists into one
        val combinedList = lists.flatMap { it }

        // Create a map to hold unique languages with merged supportKitTypes
        val languageMap = mutableMapOf<String, Language>()

        for (language in combinedList) {
            if (languageMap.containsKey(language.code.uppercase())) {
                // If the language code already exists, merge the supportKitTypes
                val existingLanguage: Language? = languageMap[language.code.uppercase()]
                existingLanguage?.let {
                    val mergedSupportKitTypes = (existingLanguage.supportKitTypes + language.supportKitTypes).distinct().toMutableList()
                    languageMap[language.code.uppercase()] = Language(existingLanguage.code).apply { supportKitTypes.addAll(mergedSupportKitTypes) }
                }
            } else {
                // Otherwise, add the language to the map
                languageMap[language.code.uppercase()] = language
            }
        }

        // Convert the map values to a list
        return languageMap.values.toList()
    }

    private fun getTranslationKit(kitType: TranslationKitType): TranslationKit {
        return when (kitType) {
            TranslationKitType.GOOGLE -> googleWebKit
            TranslationKitType.DEEPL -> deepLKit
            TranslationKitType.AZURE -> azureKit
//            TranslationKitType.YANDEX -> yandexKit
            TranslationKitType.PAPAGO -> papagoKit
        }
    }

    fun isSupportedAsSource(kitType: TranslationKitType, code: String, targetLanguageCode: String): Boolean {
        return when (kitType) {
            TranslationKitType.GOOGLE -> googleWebKit.isSupportedAsSource(code, targetLanguageCode)
            TranslationKitType.AZURE -> azureKit.isSupportedAsSource(code, targetLanguageCode)
            TranslationKitType.DEEPL -> deepLKit.isSupportedAsSource(code, targetLanguageCode)
//            TranslationKitType.YANDEX -> yandexKit.isSupportedAsSource(code, targetLanguageCode)
            TranslationKitType.PAPAGO -> papagoKit.isSupportedAsSource(code, targetLanguageCode)
        }
    }

    fun isSupportedAsTarget(kitType: TranslationKitType, code: String, sourceLanguageCode: String): Boolean {
        return when (kitType) {
            TranslationKitType.GOOGLE -> googleWebKit.isSupportedAsTarget(code, sourceLanguageCode)
            TranslationKitType.AZURE -> azureKit.isSupportedAsTarget(code, sourceLanguageCode)
            TranslationKitType.DEEPL -> deepLKit.isSupportedAsTarget(code, sourceLanguageCode)
//            TranslationKitType.YANDEX -> yandexKit.isSupportedAsTarget(code)
            TranslationKitType.PAPAGO -> papagoKit.isSupportedAsTarget(code, sourceLanguageCode)
        }
    }

    fun isLanguageSwappable(sourceLanguageCode: String, targetLanguageCode: String, kitType: TranslationKitType): Boolean {
        return when (kitType) {
            TranslationKitType.GOOGLE -> googleWebKit.isLanguageSwappable(sourceLanguageCode, targetLanguageCode)
            TranslationKitType.DEEPL -> deepLKit.isLanguageSwappable(sourceLanguageCode, targetLanguageCode)
            TranslationKitType.AZURE -> azureKit.isLanguageSwappable(sourceLanguageCode, targetLanguageCode)
//            TranslationKitType.YANDEX -> yandexKit.isLanguageSwappable(sourceLanguageCode, targetLanguageCode)
            TranslationKitType.PAPAGO -> papagoKit.isLanguageSwappable(sourceLanguageCode, targetLanguageCode)
        }
    }

    suspend fun request(
        translationKitType: TranslationKitType,
        sourceLanguageCode: String,
        targetLanguageCode: String,
        sourceText: String,
        correctedText: String? = null,
    ): TranslationResponse {
        val translationKit: TranslationKit = getTranslationKit(translationKitType)
        return translationKit.request(
            sourceLanguageCode,
            targetLanguageCode,
            correctedText ?: sourceText
        )
    }

    private fun close() {
//        googleMlKit.close()
    }

    override fun onZeroReferences() {
        close()
    }
}
