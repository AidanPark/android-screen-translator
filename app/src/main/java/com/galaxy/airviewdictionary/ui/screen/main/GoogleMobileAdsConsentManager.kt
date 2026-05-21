package com.galaxy.airviewdictionary.ui.screen.main

import android.app.Activity
import android.content.Context

/**
 * The Google Mobile Ads SDK provides the User Messaging Platform (Google's IAB Certified consent
 * management platform) as one solution to capture consent for users in GDPR impacted countries.
 * This is an example and you can choose another consent management platform to capture consent.
 */
class GoogleMobileAdsConsentManager private constructor(context: Context) {
  data class FormError(val errorCode: Int = 0, val message: String = "")

  fun interface OnConsentFormDismissedListener {
    fun onConsentFormDismissed(error: FormError?)
  }

  /** Interface definition for a callback to be invoked when consent gathering is complete. */
  fun interface OnConsentGatheringCompleteListener {
    fun consentGatheringComplete(error: FormError?)
  }

  /** Helper variable to determine if the app can request ads. */
  val canRequestAds: Boolean
    get() = false

  /** Helper variable to determine if the privacy options form is required. */
  val isPrivacyOptionsRequired: Boolean
    get() = false

  /**
   * Helper method to call the UMP SDK methods to request consent information and load/show a
   * consent form if necessary.
   */
  fun gatherConsent(
    activity: Activity,
    onConsentGatheringCompleteListener: OnConsentGatheringCompleteListener,
  ) {
    onConsentGatheringCompleteListener.consentGatheringComplete(null)
  }

  /** Helper method to call the UMP SDK method to show the privacy options form. */
  fun showPrivacyOptionsForm(
    activity: Activity,
    onConsentFormDismissedListener: OnConsentFormDismissedListener,
  ) {
    onConsentFormDismissedListener.onConsentFormDismissed(null)
  }

  companion object {
    @Volatile private var instance: GoogleMobileAdsConsentManager? = null

    fun getInstance(context: Context) =
      instance
        ?: synchronized(this) {
          instance ?: GoogleMobileAdsConsentManager(context).also { instance = it }
        }
  }
}
