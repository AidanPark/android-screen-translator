package com.galaxy.airviewdictionary.data.remote.translation.openai

import okhttp3.RequestBody
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.Headers
import retrofit2.http.POST

/**
 * OpenAI Chat Completions API. 전용 번역 엔드포인트가 없으므로 chat completion 에 번역 프롬프트를 보낸다.
 * 인증은 호출마다 `Authorization: Bearer <key>` 헤더로 전달한다.
 */
interface OpenAiService {

    @Headers("Content-Type: application/json")
    @POST("v1/chat/completions")
    suspend fun chatCompletion(
        @Header("Authorization") authorization: String,
        @Body body: RequestBody,
    ): OpenAiChatResponse
}

data class OpenAiChatResponse(val choices: List<OpenAiChoice>)
data class OpenAiChoice(val message: OpenAiMessage)
data class OpenAiMessage(val role: String, val content: String)
