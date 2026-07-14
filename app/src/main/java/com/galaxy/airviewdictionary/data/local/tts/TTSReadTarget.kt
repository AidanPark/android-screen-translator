package com.galaxy.airviewdictionary.data.local.tts

/**
 * TTS 읽기 대상.
 * 스피커 버튼(수동 재생)과 번역 자동 읽기 모두 이 설정을 따르고,
 * 활성 TTS 목소리도 선택된 대상의 언어에 연동된다.
 *
 * - SOURCE: 번역 원문(소스 텍스트)을 읽는다
 * - TARGET: 번역 결과(타겟 텍스트)를 읽는다
 */
enum class TTSReadTarget {
    SOURCE,
    TARGET,
}
