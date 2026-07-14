package com.galaxy.airviewdictionary.data.local.ads

/**
 * 광고 게이트 세션 상태 (메모리 전용 → 프로세스 종료 시 자동 리셋).
 *
 * 정책:
 *  - 리워드 광고를 끝까지 시청하면 [adFreeSession] = true → 앱 종료까지 광고 없이 사용.
 *  - 광고를 스킵하거나 로드/표시에 실패하면 [grantWindow] 로 5분 사용권만 부여 → 만료되면 다시 광고.
 */
object AdGateState {

    private const val SKIP_GRANT_MINUTES = 5

    /** 광고 완주로 이번 세션 동안 광고 없이 사용 가능한 상태. */
    @Volatile
    var adFreeSession: Boolean = false
        private set

    /** 스킵/실패로 부여된 임시 사용권 만료 시각(epoch millis). */
    @Volatile
    private var usableUntilMillis: Long = 0L

    /** 광고를 끝까지 시청함 → 세션 내내 광고 없음. */
    fun grantAdFreeSession() {
        adFreeSession = true
    }

    /** 스킵/실패 → 5분 임시 사용권 부여. */
    fun grantSkipWindow() {
        usableUntilMillis = System.currentTimeMillis() + SKIP_GRANT_MINUTES * 60_000L
    }

    /** 지금 광고 없이 사용 가능한지. */
    fun isUsable(): Boolean = adFreeSession || System.currentTimeMillis() < usableUntilMillis

    /** 부여된 임시 사용권 잔여 밀리초(없으면 0). */
    fun remainingWindowMillis(): Long =
        if (adFreeSession) Long.MAX_VALUE else (usableUntilMillis - System.currentTimeMillis()).coerceAtLeast(0L)
}
