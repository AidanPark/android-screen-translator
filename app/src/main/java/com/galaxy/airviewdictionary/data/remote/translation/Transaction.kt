package com.galaxy.airviewdictionary.data.remote.translation

data class Transaction(
    val sourceLanguageCode: String? = null,
    val targetLanguageCode: String? = null,
    val sourceText: String? = null,
    val translationKitType: TranslationKitType? = null,
    val detectedLanguageCode: String? = null,
    val resultText: String? = null,
) {
    override fun toString(): String {
        return "Translation(" +
                "sourceLanguageCode=$sourceLanguageCode, " +
                "targetLanguageCode=$targetLanguageCode, " +
                "sourceText=$sourceText, " +
                "translationKitType=$translationKitType, " +
                "detectedLanguageCode=$detectedLanguageCode, " +
                "resultText=$resultText, " +
                ")"
    }
}