package com.galaxy.airviewdictionary.ui.screen.overlay.menubar

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.galaxy.airviewdictionary.data.local.preference.PreferenceRepository
import com.galaxy.airviewdictionary.data.local.vision.TextDetectMode
import com.galaxy.airviewdictionary.data.remote.translation.TranslationKitType
import com.galaxy.airviewdictionary.data.remote.translation.TranslationRepository
import com.galaxy.airviewdictionary.data.remote.translation.Language
import com.galaxy.airviewdictionary.ui.screen.overlay.languagelist.LanguageListView
import kotlinx.coroutines.launch
import timber.log.Timber


@Suppress("UNCHECKED_CAST")
class MenuBarViewModelFactory(
    private val applicationContext: Context,
    private val preferenceRepository: PreferenceRepository,
    private val translationRepository: TranslationRepository,
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MenuBarViewModel::class.java)) {
            return MenuBarViewModel(
                applicationContext = applicationContext,
                preferenceRepository = preferenceRepository,
                translationRepository = translationRepository
            ) as T
        }
        throw IllegalArgumentException("Unknown ViewModel Class")
    }
}

class MenuBarViewModel(
    private val applicationContext: Context,
    val preferenceRepository: PreferenceRepository,
    val translationRepository: TranslationRepository
) : ViewModel() {

    private val TAG = javaClass.simpleName

    fun launchLanguageListView(isSourceLanguage: Boolean) {
        viewModelScope.launch {
            LanguageListView.INSTANCE.cast(applicationContext, if(isSourceLanguage) LanguageListView.Type.SOURCE else LanguageListView.Type.TARGET)
        }
    }

    fun isLanguageSwappable(sourceLanguageCode: String, targetLanguageCode: String, kitType: TranslationKitType): Boolean {
        return translationRepository.isLanguageSwappable(sourceLanguageCode, targetLanguageCode, kitType)
    }


    ////////////////////////////////////////////////////////////////////////////////////////////////
    //                                                                                            //
    //                                       preference                                           //
    //                                                                                            //
    ////////////////////////////////////////////////////////////////////////////////////////////////

//    fun updateTextDetectModeShown() {
//        preferenceRepository.update(PreferenceRepository.IS_HELP_TEXT_DETECT_MODE_SHOWN, true)
//    }
//
//    fun updateTranslationKitShown() {
//        preferenceRepository.update(PreferenceRepository.IS_HELP_TRANSLATION_KIT_SHOWN, true)
//    }

    fun updateTranslationKitType(kitType: TranslationKitType) {
        preferenceRepository.update(PreferenceRepository.TRANSLATION_KIT_TYPE, kitType.name)
    }

    fun updateTextDetectMode(textDetectMode: TextDetectMode) {
        preferenceRepository.update(PreferenceRepository.TEXT_DETECT_MODE, textDetectMode.name)
    }

    fun updateSwapLanguage(currentSourceLanguage: Language, currentTargetLanguage: Language) {
        preferenceRepository.update(PreferenceRepository.SOURCE_LANGUAGE_CODE, currentTargetLanguage.code)
        preferenceRepository.addOrUpdateLanguageHistory(currentTargetLanguage.code, true)
        preferenceRepository.update(PreferenceRepository.TARGET_LANGUAGE_CODE, currentSourceLanguage.code)
        preferenceRepository.addOrUpdateLanguageHistory(currentSourceLanguage.code, false)
    }

    init {
        Timber.tag(TAG).i("#### init ####")
        translationRepository.acquire()
    }

    override fun onCleared() {
        translationRepository.release()
        super.onCleared()
    }
}










