package com.galaxy.airviewdictionary.data.remote.translation

data class Transaction(
    val sourceLanguageCode: String? = null,
    val targetLanguageCode: String? = null,
    val sourceText: String? = null,
    val translationKitType: TranslationKitType? = null,
    val detectedLanguageCode: String? = null,
    val resultText: String? = null,
    // LLM 기반 엔진(OpenAI/Gemini)에서 실제 사용한 모델명. 그 외 엔진은 null.
    val modelName: String? = null,
) {
    override fun toString(): String {
        return "Translation(" +
                "sourceLanguageCode=$sourceLanguageCode, " +
                "targetLanguageCode=$targetLanguageCode, " +
                "sourceText=$sourceText, " +
                "translationKitType=$translationKitType, " +
                "detectedLanguageCode=$detectedLanguageCode, " +
                "resultText=$resultText, " +
                "modelName=$modelName, " +
                ")"
    }
}