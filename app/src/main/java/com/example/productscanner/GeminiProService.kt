package com.example.productscanner

import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.Headers
import retrofit2.http.POST

interface GeminiProService {
    @POST("/v1beta/models/gemini-1.5-flash:generateContent?key=***********************")
    @Headers("Content-Type: application/json")
    fun processImage(@Body request: GeminiProRequest): Call<GeminiProResponse>
}
