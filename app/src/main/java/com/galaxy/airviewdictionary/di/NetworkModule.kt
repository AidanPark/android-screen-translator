package com.galaxy.airviewdictionary.di

import com.galaxy.airviewdictionary.data.remote.translation.goolge.GoogleWebKit
import com.galaxy.airviewdictionary.data.remote.translation.goolge.GoogleWebService
import com.galaxy.airviewdictionary.data.remote.translation.openai.OpenAiKit
import com.galaxy.airviewdictionary.data.remote.translation.openai.OpenAiService
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import timber.log.Timber
import java.util.concurrent.TimeUnit
import javax.inject.Qualifier
import javax.inject.Singleton


@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class GoogleWebRetrofit

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class OpenAiRetrofit

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    @Provides
    @Singleton
    fun provideOkHttpClient(): OkHttpClient {
        return OkHttpClient().newBuilder()
            .addInterceptor(ResponseInterceptor())
            .readTimeout(20, TimeUnit.SECONDS)
            .connectTimeout(20, TimeUnit.SECONDS)
            .build()
    }

    @GoogleWebRetrofit
    @Provides
    @Singleton
    fun provideGoogleWebRetrofit(okHttpClient: OkHttpClient): Retrofit = Retrofit.Builder()
        .baseUrl(GoogleWebKit.BASE_URL)
        .client(okHttpClient)
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    @GoogleWebRetrofit
    @Provides
    @Singleton
    fun provideGoogleWebService(@GoogleWebRetrofit retrofit: Retrofit): GoogleWebService {
        return retrofit.create(GoogleWebService::class.java)
    }

    @OpenAiRetrofit
    @Provides
    @Singleton
    fun provideOpenAiRetrofit(okHttpClient: OkHttpClient): Retrofit = Retrofit.Builder()
        .baseUrl(OpenAiKit.BASE_URL)
        .client(okHttpClient)
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    @OpenAiRetrofit
    @Provides
    @Singleton
    fun provideOpenAiService(@OpenAiRetrofit retrofit: Retrofit): OpenAiService {
        return retrofit.create(OpenAiService::class.java)
    }
}

class ResponseInterceptor : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        val response = chain.proceed(request)
        Timber.tag("response.code").d("response.code %s", response.code)
        return response
    }
}
