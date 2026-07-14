package com.galaxy.airviewdictionary.ui.screen.overlay.voicelist

import android.content.Context
import android.speech.tts.Voice
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.galaxy.airviewdictionary.data.local.preference.PreferenceRepository
import com.galaxy.airviewdictionary.data.local.tts.TTSRepository
import com.galaxy.airviewdictionary.data.remote.translation.TranslationKitType
import com.galaxy.airviewdictionary.data.remote.translation.TranslationRepository
import com.galaxy.airviewdictionary.extensions.language
import com.galaxy.airviewdictionary.data.remote.translation.Language
import com.galaxy.airviewdictionary.data.remote.translation.TranslationResponse
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import timber.log.Timber


@Suppress("UNCHECKED_CAST")
class VoiceListViewModelFactory(
    private val applicationContext: Context,
    private val preferenceRepository: PreferenceRepository,
    private val translationRepository: TranslationRepository,
    private val ttsRepository: TTSRepository,
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(VoiceListViewModel::class.java)) {
            return VoiceListViewModel(
                applicationContext = applicationContext,
                preferenceRepository = preferenceRepository,
                translationRepository = translationRepository,
                ttsRepository = ttsRepository,
            ) as T
        }
        throw IllegalArgumentException("Unknown ViewModel Class")
    }
}

class VoiceListViewModel(
    private val applicationContext: Context,
    val preferenceRepository: PreferenceRepository,
    private val translationRepository: TranslationRepository,
    private val ttsRepository: TTSRepository,
) : ViewModel() {

    private val TAG = javaClass.simpleName

    fun playSampleVoice(voice: Voice, text_: String = "It's a voice like this.") {
        viewModelScope.launch {
            val ttsSpeechRate = preferenceRepository.ttsSpeechRateFlow.first()
            var text = text_

            if (voice.language.code != "en") {
                val response: TranslationResponse = translationRepository.request(
                    TranslationKitType.GOOGLE,
                    "en",
                    voice.language.code,
                    text
                )
                if (response is TranslationResponse.Success) {
                    text = response.result.resultText ?: text
                }
            }

            ttsRepository.playTestTTS(
                text,
                ttsSpeechRate,
                voice
            )
        }
    }


    ////////////////////////////////////////////////////////////////////////////////////////////////
    //                                                                                            //
    //                                       preference                                           //
    //                                                                                            //
    ////////////////////////////////////////////////////////////////////////////////////////////////

    private val ttsOrderedVoiceNamesFlow: Flow<List<String>> = preferenceRepository.ttsOrderedVoiceNamesFlow

    @OptIn(ExperimentalCoroutinesApi::class)
    val voicesFlow = ttsOrderedVoiceNamesFlow.flatMapLatest { orderedVoiceNames ->
        Timber.tag(TAG).d("orderedVoiceNames $orderedVoiceNames")
        // Step 1: 저장된 orderedVoiceNames 기준으로 Pair<Voice, Language> 리스트를 만든다.
        val voiceLanguagePairList = mutableListOf<Pair<Voice, Language>>()
        val availableVoices = ttsRepository.availableVoicesFlow.filterNotNull().first()
        if (orderedVoiceNames.isEmpty()) { // 저장된 내용이 없다면
            voiceLanguagePairList.addAll(
                availableVoices.map { voice -> voice to voice.language }
            )
        } else {
            orderedVoiceNames.mapNotNull { orderedVoice ->
                availableVoices.find { it.name == orderedVoice }?.also { voice ->
                    voiceLanguagePairList.add(voice to voice.language)
                }
            }
        }
        voiceLanguagePairList.sortBy { it.second.displayName }
        Timber.tag(TAG).d("ttsRepository.availableVoices ${availableVoices.map { voice -> voice.name }}")
        Timber.tag(TAG).d("voiceLanguagePairList ${voiceLanguagePairList.map { voice -> voice.first.name }}")

        // Step 2: ttsRepository.availableVoices 의 요소가 voiceLanguagePairList 에 없는것이 있다면 해당 언어 그룹의 마지막에 추가
        availableVoices.forEach { availableVoice ->
            if (voiceLanguagePairList.none { it.first.name == availableVoice.name }) {
                voiceLanguagePairList.findLast { it.second.displayName == availableVoice.language.displayName }
                    ?.let { lastMatching ->
                        val index = voiceLanguagePairList.indexOf(lastMatching) + 1
                        voiceLanguagePairList.add(index, availableVoice to availableVoice.language)
                    } ?: voiceLanguagePairList.add(availableVoice to availableVoice.language)
            }
        }

        // Step 3: List<Triple<Int, Voice, Language>> 반환
        flowOf(
            voiceLanguagePairList.mapIndexed { index, pair ->
                Triple(index, pair.first, pair.second)
            }
        )
    }

    fun addOrUpdateOrderedVoiceNames(orderedVoices: List<Triple<Int, Voice, Language>>) {
        viewModelScope.launch {
            // orderedVoiceNames 업데이트.
            // 활성 목소리 재적용은 TTSRepository.collectSourceLanguageVoice 가 (소스 언어 기준으로) 처리한다.
            preferenceRepository.addOrUpdateOrderedVoiceNames(orderedVoices.map { triple -> triple.second })
        }
    }

    init {
        Timber.tag(TAG).i("#### init ####")
        translationRepository.acquire()
        ttsRepository.acquire()
    }

    override fun onCleared() {
        translationRepository.release()
        ttsRepository.release()
        super.onCleared()
    }
}








