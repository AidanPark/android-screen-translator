package com.galaxy.airviewdictionary.extensions

import android.speech.tts.Voice
import com.galaxy.airviewdictionary.data.remote.translation.Language

val Voice.languageCode: String
    get() = this.name.split("-")[0]

val Voice.language: Language
    get() = Language(this.languageCode)

/**
 * TTS voice 이름("ko-kr-x-koc-network")이 주어진 언어코드의 목소리인지 판별한다.
 * 언어 세그먼트를 정확히 비교한다.
 * (startsWith prefix 비교는 "ko"(한국어)가 "kok"(콘칸어) 목소리에도 매칭되는 오류가 있다)
 */
fun voiceNameMatchesLanguage(voiceName: String, languageCode: String): Boolean =
    voiceName.substringBefore("-").equals(languageCode.substringBefore("-"), ignoreCase = true)
